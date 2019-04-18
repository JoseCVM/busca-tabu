package problems.qbfpt.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbfpt.QBFPT_Inverse;
import solutions.Solution;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #QuadracticBinaryFunction}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 *
 * @author ccavellucci, fusberti
 */
public class TS_QBFPT extends AbstractTS<Integer> {

    private final Integer fake = new Integer(-1);
    private boolean bestImproving = false;
    private boolean powerMoves = false;
    private boolean restart = true;
    private int age[] = new int[1000];
    private boolean cantRemove[] = new boolean[1000];
    private int maxAge = 50;
    private Double restartStep = 0.2;
    private Double powerMoveMin = 0.2;
    /**
     * Constructor for the TS_QBF class. An inverse QBF objective function is
     * passed as argument for the superclass constructor.
     *
     * @param tenure
     *            The Tabu tenure parameter.
     * @param iterations
     *            The number of iterations which the TS will be executed.
     * @param filename
     *            Name of the file for which the objective function parameters
     *            should be read.
     * @throws IOException
     *             necessary for I/O operations.
     */
    public TS_QBFPT(Integer tenure, Integer iterations, String filename, boolean bstImp,boolean pm,boolean rstrt) throws IOException {
        super(new QBFPT_Inverse(filename), tenure, iterations);
        this.bestImproving = bstImp;
        this.powerMoves = pm;
        this.restart = rstrt;
        for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {
        	age[i] = 0;
        	cantRemove[i] = false;
        }
    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeCL()
     */
    @Override
    public ArrayList<Integer> makeCL() {

        ArrayList<Integer> _CL = new ArrayList<Integer>();
        for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
            Integer cand = new Integer(i);
            _CL.add(cand);
        }

        return _CL;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
     */
    @Override
    public ArrayList<Integer> makeRCL() {

        ArrayList<Integer> _RCL = new ArrayList<Integer>();

        return _RCL;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeTL()
     */
    @Override
    public ArrayDeque<Integer> makeTL() {

        ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
        for (int i=0; i<2*tenure; i++) {
            _TS.add(fake);
        }

        return _TS;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#updateCL()
     */
    @Override
    public void updateCL() {

        // do nothing
		this.CL = this.ObjFunction.GetCL(this.incumbentSol);

    }

    /**
     * {@inheritDoc}
     *
     * This createEmptySol instantiates an empty solution and it attributes a
     * zero cost, since it is known that a QBF solution with all variables set
     * to zero has also zero cost.
     */
    @Override
    public Solution<Integer> createEmptySol() {
        Solution<Integer> sol = new Solution<Integer>();
        sol.cost = 0.0;
        return sol;
    }

    /**
     * {@inheritDoc}
     *
     * The local search operator developed for the QBF objective function is
     * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
     */
    @Override
    public Solution<Integer> neighborhoodMove() {

        int maxAgeQt = 0;
        for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {
        	//System.out.println(i+" "+age[i]);
        	if(this.incumbentSol.contains(i)) age[i] += 1;
        	else age[i] = 0;
        	if(age[i] > this.maxAge) maxAgeQt++;
        }
        Double maxAgePct =  (double) ( (double)maxAgeQt/ (double)(this.ObjFunction.getDomainSize()));
        //System.out.println(maxAgePct);
        if(this.restart){
        	if(maxAgePct > this.restartStep) {
        		System.out.println("Intensifica");
        		this.restartStep += 0.1;
        		for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {                	
                	if(age[i] > this.maxAge) this.cantRemove[i] = true;
                }
            	TL.clear();
        	}        

            if(this.restartStep > 0.4) {
        		System.out.println("Para de intensificar e relaxa");
        		for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {                	
                	this.cantRemove[i] = false;
                	this.age[i] = 0;
                }
        		this.restartStep = 0.0;
        	}
        }
        Double minDeltaCost;
        Integer bestCandIn = null, bestCandOut = null;

        minDeltaCost = Double.POSITIVE_INFINITY;
        updateCL();
        
        Collections.shuffle(CL); 
        Collections.shuffle(incumbentSol);
        
        Integer doubleIn1=null,doubleIn2=null,doubleRem1=null,doubleRem2=null;
        if(this.powerMoves && maxAgePct > this.powerMoveMin) {
        	System.out.println("Power move");        	
    		this.powerMoveMin += 0.1;
    		System.out.println("Intensifica");
        	TL.clear();        
    		for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {                	
            	if(age[i] > this.maxAge) this.cantRemove[i] = true;
            }	        	
        	for (Integer candIn : CL) {
                if (TL.contains(candIn)) continue;
                Solution<Integer> nsl = new Solution<Integer>(incumbentSol);
                nsl.add(candIn);
                ArrayList<Integer> nCl = this.ObjFunction.GetCL(nsl);
                for(Integer candIn2 : nCl) {
                    Double deltaCost = ObjFunction.evaluateInsertionCost(candIn2, nsl);
                    if((TL.contains(candIn2) || TL.contains(candIn)) && !(nsl.cost+deltaCost < bestSol.cost)) continue;
                    if(nsl.cost+deltaCost < bestSol.cost ) {
                    	minDeltaCost = deltaCost;
                        doubleIn1 = candIn;
                        doubleIn2 = candIn2;
                        doubleRem1 = null;
                        doubleRem2 = null;
                        if(this.bestImproving == false) break;
                    }
                }
            }
        	
        	for (Integer candOut : incumbentSol) {
	        	if(cantRemove[candOut]) continue;	            
                Solution<Integer> nsl = new Solution<Integer>(incumbentSol);
                nsl.remove(candOut);
                for(Integer candOut2 : nsl) {
                    Double deltaCost = ObjFunction.evaluateRemovalCost(candOut2, nsl);
                    if((TL.contains(candOut2) || TL.contains(candOut)) && !(nsl.cost+deltaCost < bestSol.cost)) continue;
    	            if (deltaCost < minDeltaCost) {
	                    minDeltaCost = deltaCost;
	                    doubleIn1 = null;
                        doubleIn2 = null;
                        doubleRem1 = candOut;
                        doubleRem2 = candOut2;
	                    if(this.bestImproving == false) break;
	                }
                }
	        }

            if(this.powerMoveMin > 0.4) {
        		System.out.println("Para de intensificar e relaxa");
        		for(int i = 0;i<this.ObjFunction.getDomainSize();i++) {                	
                	this.cantRemove[i] = false;
                	this.age[i] = 0;
                }
        		this.powerMoveMin = 0.2;
        	}
        }else {
	        // Evaluate insertions
	        for (Integer candIn : CL) {
	            Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, incumbentSol);
	            if (!TL.contains(candIn) || incumbentSol.cost+deltaCost < bestSol.cost) {
	                if (deltaCost < minDeltaCost) {
	                    minDeltaCost = deltaCost;
	                    bestCandIn = candIn;
	                    bestCandOut = null;
	                    if(this.bestImproving == false) break;
	                }
	            }
	        }
	        // Evaluate removals
	        for (Integer candOut : incumbentSol) {
	        	if(cantRemove[candOut]) continue;
	            Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, incumbentSol);
	            if (!TL.contains(candOut) || incumbentSol.cost+deltaCost < bestSol.cost) {
	                if (deltaCost < minDeltaCost) {
	                    minDeltaCost = deltaCost;
	                    bestCandIn = null;
	                    bestCandOut = candOut;
	                    if(this.bestImproving == false) break;
	                }
	            }
	        }
        }
        // Evaluate exchanges
        for (Integer candIn : CL) {
            boolean stop = false;
            for (Integer candOut : incumbentSol) {
            	if(cantRemove[candOut]) continue;
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, incumbentSol);
                if ((!TL.contains(candIn) && !TL.contains(candOut)) || incumbentSol.cost+deltaCost < bestSol.cost) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                        if (this.bestImproving == false) {
                            stop = true;
                            break;
                        }
                    }
                }
            }
            if(stop == true) break;
        }
        // Implement the best non-tabu move
        TL.poll();
        if (bestCandOut != null) {
            incumbentSol.remove(bestCandOut);
            CL.add(bestCandOut);
            TL.add(bestCandOut);
        } else {
            TL.add(fake);
        }
        TL.poll();
        if (bestCandIn != null) {
            incumbentSol.add(bestCandIn);
            CL.remove(bestCandIn);
            TL.add(bestCandIn);
        } else {
            TL.add(fake);
        }
        if(this.powerMoves) {
        	TL.poll();
        	TL.poll();
        	if(doubleIn1 != null) {
        		incumbentSol.add(doubleIn1);
        		incumbentSol.add(doubleIn2);
        		CL.remove(doubleIn1);
        		CL.remove(doubleIn2);
        		TL.add(doubleIn1);
        		TL.add(doubleIn2);
        	}else {
        		TL.add(fake);
        		TL.add(fake);
        	}
        	TL.poll();
        	TL.poll();
        	if(doubleRem1 != null) {
        		incumbentSol.remove(doubleRem1);
        		incumbentSol.remove(doubleRem2);
        		CL.add(doubleRem1);
        		CL.add(doubleRem2);
        		TL.add(doubleRem1);
        		TL.add(doubleRem2);
        	}else {
        		TL.add(fake);
        		TL.add(fake);
        	}
        }
        ObjFunction.evaluate(incumbentSol);
        return null;
    }

    /**
     * A main method used for testing the TS metaheuristic.
     *
     */
    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        TS_QBFPT tabusearch = new TS_QBFPT(10, 30000, "C:\\Users\\josec\\tabu-search\\TS_Framework\\instances\\qbf400",true,true,false);
        Solution<Integer> bestSol = tabusearch.solve();
        String s = "";
        for(int i = 0;i < tabusearch.ObjFunction.getDomainSize();i++) {
        	if(tabusearch.bestSol.contains(i)) s+= "1 ";
        	else s+= "0 ";
        }
        System.out.println(s);
        System.out.println("maxVal = " + bestSol);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

    }

}
