package forge;

import arc.Core;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.*;
import arc.util.Log;
import mindustry.gen.Call;
import mindustry.io.SaveFileReader;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

//Class which stores and controls the heat in both blocks and floors and their flow.
public class TileHeatControl implements SaveFileReader.CustomChunk {

    static TileHeatControl instance;

    public static TileHeatControl getInstance() {
        if (instance == null) instance = new TileHeatControl();
        return instance;
    }

    public TileHeatControl(){

    }

    public static TileHeatSetup setup;

    static HeatState tmpS1;

    //Ambient Temperature in celsius
    public static float ambientTemperature = 303.15f,
    //How conductive the atmosphere is
    envTempChange = 0.3f;

    public static MaterialPreset tmpMP1 = new MaterialPreset(), tmpMP2 = new MaterialPreset();

    //
    public static MaterialPreset defaultFloor = new MaterialPreset(0.12f, 1),
            defaultBlock = new MaterialPreset(0.07f, 3),
            defaultAir = new MaterialPreset(0.4f, 0.6f);
    public static float simulationSpeed = 1;
    public static boolean enabled;

    public boolean gridLoaded = false;

    private static final ArrayList<MaterialPreset> presetList = new ArrayList<>();


    //Map storing all grid states
    public static Seq<HeatState> gridStates;
    //Sequence storing all non-grid tile states.
    public static Seq<HeatState> entityStates;

    public HeatRunnerThread heatThread;
    public int w, h, s;

    public void setupThread(){
        heatThread = new TileHeatControl.HeatRunnerThread();
        heatThread.setPriority(Thread.NORM_PRIORITY - 1);
        heatThread.setDaemon(true);
        heatThread.start();
        Log.info("Started Heat");
    }
    public void setTileValues(int index, float energy, float mass, MaterialPreset material){
        tmpS1 = gridStates.get(index);
        tmpS1.energy = energy;
        tmpS1.mass = mass;
        tmpS1.material = material;
    }
    public static class MaterialPreset{
        public float
                //How conductive the material is. More Thermal Conductivity means more flow of heat between the tile and it's neighbours.
                // Conductivity works on averages, so something with almost no conductivity next to something with high conductivity will still conduct heat.
                thermalConductivity,
                //How much energy it takes to raise one unit of mass one kelvin. This one is self-explanatory.
                specificHeatCapacity;
        
        public MaterialPreset(){

        }

        public MaterialPreset(float thermalConductivity, float specificHeatCapacity){
            this.thermalConductivity = thermalConductivity;
            this.specificHeatCapacity = specificHeatCapacity;
        }
    }

    public void start(int width, int height){
        initializeValues();
        createGrid(width, height);
        setup.setupGrid(this);
        gridLoaded = true;
    }

    public float getEnergy(int x, int y, boolean floor){
        return getEnergy(x + y * w + (floor ? s : 0));
    }

    public float getEnergy(int index){
        return gridStates.get(index).energy;
    }

    public float getMass(int x, int y, boolean floor){
        return getMass(x + y * w + (floor ? s : 0));
    }

    public float getMass(int index){
        return gridStates.get(index).mass;
    }
    public void createGrid(int w, int h){
        this.w = w;
        this.h = h;
        //How large a single grid is on the array. This is not final, and can be changed for custom implementations.
        s = w * h;
        gridStates = new Seq<>(true, s);
        entityStates = new Seq<>(false);

        //Set up map and neighbours
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int index = x + y * w;
                tmpS1 = new HeatState();
                gridStates.add(tmpS1);
                tmpS1.updates = new Seq();
            }
        }

        //Setup neighbours. Being rewritten to account for multiblocks soon.
        for (int i = 0; i < s; i++) {
            HeatState current = gridStates.get(i);
            //Starts halfway in 2 in to maintain update order. Tiles should update against the direction iteration is going in to avoid needing to iterating over flow values a second time.
            for (int j = 2; j < 4; j++) {
                int sideIndex = i + Geometry.d4x(j) + Geometry.d4y(j) * w;
                if(sideIndex < 0 || sideIndex >= s) {
                    continue;
                }
                //Set up tile with their respective neighbours
                HeatState neighbour = gridStates.get(sideIndex);
                current.updates.add(neighbour);
            }
        }
    }

    public void tick(){
        Log.info("Ticking");
        setup.update(this);
        updateFlow();
    }

    //Note that ambient heat is factored in after flow calculations
    public void updateFlow()
    {
        gridStates.each(HeatState::updateState);
        entityStates.each(HeatState::updateState);
    }

    //Note that this ignores surface area. That logic should be implemented in the object calling this
    public static float calculateFlow(float mass1, float mass2, float temp1, float temp2, MaterialPreset preset1, MaterialPreset preset2){

        //Debug.Log("Going from tile: " + tile1 + " to " + tile2);

        //Don't transfer heat if either masses are below 1 g. Obviously this should never happen but eh
        if (mass1 + mass2 < 2) {
            return 0;
        }

        float tempretureDif = temp2 - temp1;

        //Don't bother calculating if tempreture difference is less than 1 celcius
        if (Math.abs(tempretureDif) < 1f) {
            return 0;
        }

        float geomThermalConductivity = Mathf.sqrt(preset1.thermalConductivity * preset2.thermalConductivity);
        float flowAmount = geomThermalConductivity * tempretureDif * simulationSpeed;

        //Don't bother using if energy flow is less than 0.1 units
        if (Math.abs(flowAmount) < 0.1f) return 0;

        //Cap change of energy to 1/5 of the temp difference changed per tick
        float maxTempDif = Math.min(Math.abs(tempretureDif/5 * mass1 * preset1.specificHeatCapacity),
                Math.abs(tempretureDif / 5 * mass2 * preset2.specificHeatCapacity));

        return Mathf.clamp(flowAmount, -maxTempDif, maxTempDif);
    }

    //Note that this assumes all "faces" are uniformly sized and in constant contact with the atmosphere for the full duration between heat ticks. Accounting for this would add unescecery bloat to this relatively simple method.
    public static float calculateFlowAtmosphere(float mass, float temp, MaterialPreset preset, int faces){
        
        float tempretureDif = ambientTemperature - temp;
        float geomThermalConductivity = Mathf.sqrt(preset1.thermalConductivity * envTempChange);
        //Simple change allowing faces to act as a multiplier, while still being clamped in the result.
        float flowAmount = geomThermalConductivity * tempretureDif * faces * simulationSpeed;
        
        //Cap change of energy to 1/5 of the temp difference changed per tick based only on the mass interacting with the atmosphere.
        float maxTempDif = Math.abs(tempretureDif/5 * mass1 * preset1.specificHeatCapacity);

        
        return Mathf.clamp(flowAmount, -maxTempDif, maxTempDif);
    }
    
    public void initializeValues(){

    }
    public static float kelvins(HeatState state){
        return kelvins(state.energy, state.mass, state.material.specificHeatCapacity);
    }

    public static float celsius(HeatState state){
        return kelvins(state) - 273.15f;
    }
    public static float kelvins(float energy, float mass, float SPH){
        return energy/(mass*SPH);
    }

    public static float celsius(float energy, float mass, float SPH){
        return kelvins(energy, mass, SPH) - 273.15f;
    }

    @Override
    public void write(DataOutput stream) throws IOException {
        stream.writeBoolean(enabled);
        if(!enabled) return;
        for (int i = 0; i < s; i++) {
            tmpS1 = gridStates.get(s);
            stream.writeFloat(tmpS1.energy);
            stream.writeFloat(tmpS1.mass);
        }
    }

    public void writeState(DataOutput stream, HeatState state){
    }

    @Override
    public void read(DataInput stream) throws IOException {
        enabled = stream.readBoolean();
        if(!enabled) return;
        for (int i = 0; i < s * 2; i++) {
            tmpS1 = gridStates.get(s);
            tmpS1.energy = stream.readFloat();
            tmpS1.mass = stream.readFloat();
        }
    }

    /**
     * A class that stores energy, mass and incing energy.
     * NOTE THAT WHEN ADDING TO THE STATE'S ENERGY, USE FLOW INSTEAD OF ENERGY.
     */
    public static class HeatState {

        public HeatState(){

        }
        public float energy, mass, flow, lastFlow;

        public int faces;

        public boolean shielded;
        
        public MaterialPreset material;

        //Note: Update order is important
        public Seq<HeatState> updates;

        public void updateState(){
            
            if(!shielded) flow = calculateFlowAtmosphere(mass, kelvins(this), material, faces);
            
            updates.each(n -> {
                float flow = calculateFlow(mass, n.mass, kelvins(this), kelvins(n), material, n.material);

                this.flow += flow;
                n.flow -= flow;
            });

            //Add incoming flow to tile. Note that update order matters.
            energy += flow;
            //For debugging purposes
            lastFlow = flow;

            flow = 0;
        }
    }

    //Coppied from Xelo, yoinky~
    //I swear I  half understand how it works
    public class HeatRunnerThread extends Thread {
        boolean terminate, doStep;
        public int currentTime;
        public float targetTime;
        final Object waitSync = new Object();

        @Override
        public void run() {
            super.run();
            Log.info("--- Heat runner thread started");
            try {
                while (!terminate) {
                    while (!doStep) {
                        Thread.sleep(16);
                        if (Core.app.isDisposed()) {
                            Log.info(" >>>>> Thread terminated due to app"); // we have to busy wait bc theres no hook for app termination
                            return;
                        }
                    }

                    while (currentTime < targetTime) {
                        tick();
                        synchronized (waitSync) {
                            currentTime++;
                        }
                    }
                    doStep = false;
                }
            }
            catch(
            InterruptedException e)
            {
                terminate = true;
                Log.info(" >>>>> Thread terminated");
                return;
            }catch(Exception e){
                Log.debug(e);
                Log.debug(Arrays.asList(e.getStackTrace()).toString());
                Call.sendChatMessage(e.toString());
                Log.info(" >>>>> Thread terminated");
                return;
            }
        }

        public void updateTime(float delta){
            targetTime += delta;
            if(targetTime > currentTime){
                doStep = true;
            }
        }
    };
}
