package problems.qbfpt.solver.multithread;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import problems.qbfpt.solvers.TS_QBFPT;
import solutions.Solution;

public class TS_QBFPT_multithreaded extends TS_QBFPT implements Runnable{

	public TS_QBFPT_multithreaded(Integer tenure, Integer iterations, String filename, boolean bstImp, boolean pm,
			boolean rstrt) throws IOException {
		super(tenure, iterations, filename, bstImp, pm, rstrt);
		this.done = false;
		}
		public Solution<Integer> finalSolution;	
		public long totalTime;
		public Boolean done;
		public void run() {
			try {
				long startTime = System.currentTimeMillis();
				this.finalSolution = this.solve();
				this.totalTime = System.currentTimeMillis() - startTime;
				this.done = true;
			}catch(Exception e){
				System.out.println("Espero nunca ver isso.");
			}
		}
		public void displaySolution() {
			System.out.println("maxVal = " + bestSol);
			System.out.println("Time = "+(double)totalTime/(double)1000+" seg");
		}
		public static void main(String args[]) throws IOException {
			String instances[] = {"TS_Framework/instances/qbf020","TS_Framework/instances/qbf040","TS_Framework/instances/qbf060","TS_Framework/instances/qbf080","TS_Framework/instances/qbf100","TS_Framework/instances/qbf200","TS_Framework/instances/qbf400"};
			Integer sizes[] = {20,40,60,80,100,200,400};
			
			TS_QBFPT_multithreaded.verbose = false;
			int maxIter = 30 * 60 * 1000; // 30 min
			for(int i = 0;i<7;i++) {
				System.out.println("Instancia: "+instances[i]);
				
				TS_QBFPT_multithreaded tsPadrao = new TS_QBFPT_multithreaded(10, maxIter, instances[i], false,false,false);
				TS_QBFPT_multithreaded tsDiffTenure = new TS_QBFPT_multithreaded(15, maxIter, instances[i], false,false,false);
				TS_QBFPT_multithreaded tsBestImprov = new TS_QBFPT_multithreaded(10, maxIter, instances[i],true,false,false);
				TS_QBFPT_multithreaded tsIntNeighborhood = new TS_QBFPT_multithreaded(10, maxIter, instances[i], false,true,false);
				TS_QBFPT_multithreaded tsIntRestart = new TS_QBFPT_multithreaded(10, maxIter, instances[i], false,false,true);
				TS_QBFPT_multithreaded tsVariableTenure = new TS_QBFPT_multithreaded((int) (sizes[i]*0.2), maxIter, instances[i], false,false,false);
				
				Thread tsPadraoThread = new Thread(tsPadrao);
				Thread tsDiffTenureThread = new Thread(tsDiffTenure);
				Thread tsBestImprovThread = new Thread(tsBestImprov);
				Thread tsIntNeighborhoodThread = new Thread(tsIntNeighborhood);
				Thread tsIntRestartThread = new Thread(tsIntRestart);
				Thread tsVariableTenureThread = new Thread(tsVariableTenure);
				
				tsPadraoThread.start();
				tsDiffTenureThread.start();
				tsBestImprovThread.start();
				tsIntNeighborhoodThread.start();
				tsIntRestartThread.start();
				tsVariableTenureThread.start();
				
				try {
					TimeUnit.MILLISECONDS.sleep((int)(maxIter * 1.1));
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
							
				System.out.println("TS padrao: ");
				tsPadrao.displaySolution();
				System.out.println("");
				System.out.println("TS tenure mudado: ");
				tsDiffTenure.displaySolution();
				System.out.println("");
				System.out.println("TS best improving: ");
				tsBestImprov.displaySolution();
				System.out.println("");
				System.out.println("TS Neighbourhood intensification: ");
				tsIntNeighborhood.displaySolution();
				System.out.println("");
				System.out.println("TS restart intensification: ");
				tsIntRestart.displaySolution();
				System.out.println("");
				System.out.println("TS variable tenure: ");
				tsVariableTenure.displaySolution();
				System.out.println("");
				System.out.println("");
			}
		
		}

}
