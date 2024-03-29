package problems.qbfpt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import problems.Evaluator;
import solutions.Solution;

public class QBFPT extends Evaluator<Integer> {

    /**
     * Dimension of the domain.
     */
    public final Integer size;

    /**
     * The array of numbers representing the domain.
     */
    public final Double[] variables;

    /**
     * The matrix A of coefficients for the QBFPT f(x) = x'.A.x
     */
    public Double[][] A;

    /**
     * The list T of prohibited tuples
     */
    public Integer[][] prohibited_triples;

    /**
     * The constructor for QuadracticBinaryFunction class. The filename of the
     * input for setting matrix of coefficients A of the QBF. The dimension of
     * the array of variables x is returned from the {@link #readInput} method.
     *
     * @param filename
     *            Name of the file containing the input for setting the QBF.
     * @throws IOException
     *             Necessary for I/O operations.
     */
    public QBFPT(String filename) throws IOException {
        size = readInput(filename);
        variables = allocateVariables();
        prohibited_triples = mountProhibitedList();
    }


    public Integer[][] mountProhibitedList() {
        Integer[][] triples = new Integer[size][3];
        for (int i = 0; i < size; i++) {
            triples[i][0] = i+1;

            if (lFunction(i, 131, 1031) != i) {
                triples[i][1] = lFunction(i, 131, 1031);
            } else {
                triples[i][1] = 1 + (lFunction(i, 131, 1031) % size);
            }

            Integer x = 1 + (lFunction(i, 193, 1093) % size);
            if (lFunction(i, 193, 1093) != i && lFunction(i, 193, 1093) != triples[i][1]) {
                triples[i][2] = lFunction(i, 193, 1093);
            } else if (x != i && x != triples[i][1]) {
                triples[i][2] = x;
            } else {
                triples[i][2] = 1 + ((lFunction(i, 193, 1093) + 1) % size);
            }
            Integer maxi = Math.max(triples[i][0], Math.max(triples[i][1], triples[i][2]));
            Integer mini = Math.min(triples[i][0], Math.min(triples[i][1], triples[i][2]));
            Integer middle = triples[i][0] + triples[i][1] + triples[i][2] - maxi - mini;
            triples[i][0] = mini-1;
            triples[i][1] = middle-1;
            triples[i][2] = maxi-1;
        }
        return triples;
    }

    public void printProhibitedList() {
        for (int i = 0; i < size; i++) {
            System.out.println(prohibited_triples[i][0] + " " + prohibited_triples[i][1] + " " +  prohibited_triples[i][2]);
        }
    }

    private Integer lFunction(Integer u, Integer pi_1, Integer pi_2) {
        return 1 + ((pi_1 * u + pi_2) % size);
    }


    public ArrayList<Integer> GetCL(ArrayList<Integer> incumbentSol) {
        HashSet<Integer> sol = new HashSet<Integer> (incumbentSol);


		HashSet<Integer> CL = new HashSet<Integer>();
		for (int i = 0; i < this.getDomainSize(); i++)
			if (!sol.contains(i))
				CL.add(i);

        for (int i = 0; i < this.prohibited_triples.length; i++)
        {
            if (sol.contains(this.prohibited_triples[i][2]) && sol.contains(this.prohibited_triples[i][1]))
                CL.remove(prohibited_triples[i][0]);
            if (sol.contains(this.prohibited_triples[i][0]) && sol.contains(this.prohibited_triples[i][2]))
                CL.remove(prohibited_triples[i][1]);
            if (sol.contains(this.prohibited_triples[i][0]) && sol.contains(this.prohibited_triples[i][1]))
                CL.remove(prohibited_triples[i][2]);
        }

        return new ArrayList<Integer> (CL);
    }

    /**
     * Evaluates the value of a solution by transforming it into a vector. This
     * is required to perform the matrix multiplication which defines a QBF.
     *
     * @param sol
     *            the solution which will be evaluated.
     */
    public void setVariables(Solution<Integer> sol) {

        resetVariables();
        if (!sol.isEmpty()) {
            for (Integer elem : sol) {
                variables[elem] = 1.0;
            }
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#getDomainSize()
     */
    @Override
    public Integer getDomainSize() {
        return size;
    }

    /**
     * {@inheritDoc} In the case of a QBF, the evaluation correspond to
     * computing a matrix multiplication x'.A.x. A better way to evaluate this
     * function when at most two variables are modified is given by methods
     * {@link #evaluateInsertionQBF(int)}, {@link #evaluateRemovalQBF(int)} and
     * {@link #evaluateExchangeQBF(int,int)}.
     *
     * @return The evaluation of the QBF.
     */
    @Override
    public Double evaluate(Solution<Integer> sol) {

        setVariables(sol);
        return sol.cost = evaluateQBF();

    }

    /**
     * Evaluates a QBF by calculating the matrix multiplication that defines the
     * QBF: f(x) = x'.A.x .
     *
     * @return The value of the QBF.
     */
    public Double evaluateQBF() {

        Double aux = (double) 0, sum = (double) 0;
        Double vecAux[] = new Double[size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                aux += variables[j] * A[i][j];
            }
            vecAux[i] = aux;
            sum += aux * variables[i];
            aux = (double) 0;
        }

        return sum;

    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#evaluateInsertionCost(java.lang.Object,
     * solutions.Solution)
     */
    @Override
    public Double evaluateInsertionCost(Integer elem, Solution<Integer> sol) {

        setVariables(sol);
        return evaluateInsertionQBF(elem);

    }

    /**
     * Determines the contribution to the QBF objective function from the
     * insertion of an element.
     *
     * @param i
     *            Index of the element being inserted into the solution.
     * @return Ihe variation of the objective function resulting from the
     *         insertion.
     */
    public Double evaluateInsertionQBF(int i) {

        if (variables[i] == 1)
            return 0.0;

        return evaluateContributionQBF(i);
    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#evaluateRemovalCost(java.lang.Object,
     * solutions.Solution)
     */
    @Override
    public Double evaluateRemovalCost(Integer elem, Solution<Integer> sol) {

        setVariables(sol);
        return evaluateRemovalQBF(elem);

    }

    /**
     * Determines the contribution to the QBF objective function from the
     * removal of an element.
     *
     * @param i
     *            Index of the element being removed from the solution.
     * @return The variation of the objective function resulting from the
     *         removal.
     */
    public Double evaluateRemovalQBF(int i) {

        if (variables[i] == 0)
            return 0.0;

        return -evaluateContributionQBF(i);

    }

    /*
     * (non-Javadoc)
     *
     * @see problems.Evaluator#evaluateExchangeCost(java.lang.Object,
     * java.lang.Object, solutions.Solution)
     */
    @Override
    public Double evaluateExchangeCost(Integer elemIn, Integer elemOut, Solution<Integer> sol) {

        setVariables(sol);
        return evaluateExchangeQBF(elemIn, elemOut);

    }

    /**
     * Determines the contribution to the QBF objective function from the
     * exchange of two elements one belonging to the solution and the other not.
     *
     * @param in
     *            The index of the element that is considered entering the
     *            solution.
     * @param out
     *            The index of the element that is considered exiting the
     *            solution.
     * @return The variation of the objective function resulting from the
     *         exchange.
     */
    public Double evaluateExchangeQBF(int in, int out) {

        Double sum = 0.0;

        if (in == out)
            return 0.0;
        if (variables[in] == 1)
            return evaluateRemovalQBF(out);
        if (variables[out] == 0)
            return evaluateInsertionQBF(in);

        sum += evaluateContributionQBF(in);
        sum -= evaluateContributionQBF(out);
        sum -= (A[in][out] + A[out][in]);

        return sum;
    }

    /**
     * Determines the contribution to the QBF objective function from the
     * insertion of an element. This method is faster than evaluating the whole
     * solution, since it uses the fact that only one line and one column from
     * matrix A needs to be evaluated when inserting a new element into the
     * solution. This method is different from {@link #evaluateInsertionQBF(int)},
     * since it disregards the fact that the element might already be in the
     * solution.
     *
     * @param i
     *            index of the element being inserted into the solution.
     * @return the variation of the objective function resulting from the
     *         insertion.
     */
    private Double evaluateContributionQBF(int i) {

        Double sum = 0.0;

        for (int j = 0; j < size; j++) {
            if (i != j)
                sum += variables[j] * (A[i][j] + A[j][i]);
        }
        sum += A[i][i];

        return sum;
    }

    /**
     * Responsible for setting the QBF function parameters by reading the
     * necessary input from an external file. this method reads the domain's
     * dimension and matrix {@link #A}.
     *
     * @param filename
     *            Name of the file containing the input for setting the black
     *            box function.
     * @return The dimension of the domain.
     * @throws IOException
     *             Necessary for I/O operations.
     */
    protected Integer readInput(String filename) throws IOException {

        Reader fileInst = new BufferedReader(new FileReader(filename));
        StreamTokenizer stok = new StreamTokenizer(fileInst);

        stok.nextToken();
        Integer _size = (int) stok.nval;
        A = new Double[_size][_size];

        for (int i = 0; i < _size; i++) {
            for (int j = i; j < _size; j++) {
                stok.nextToken();
                A[i][j] = stok.nval;
                //A[j][i] = A[i][j];
                if (j>i)
                    A[j][i] = 0.0;
            }
        }

        return _size;

    }

    /**
     * Reserving the required memory for storing the values of the domain
     * variables.
     *
     * @return a pointer to the array of domain variables.
     */
    protected Double[] allocateVariables() {
        Double[] _variables = new Double[size];
        return _variables;
    }

    /**
     * Reset the domain variables to their default values.
     */
    public void resetVariables() {
        Arrays.fill(variables, 0.0);
    }

    /**
     * Prints matrix {@link #A}.
     */
    public void printMatrix() {

        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                System.out.print(A[i][j] + " ");
            }
            System.out.println();
        }

    }

    /**
     * A main method for testing the QBF class.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        QBFPT qbf = new QBFPT("instances/qbf020");
        qbf.printMatrix();
        qbf.printProhibitedList();
        Double maxVal = Double.NEGATIVE_INFINITY;

        // evaluates randomly generated values for the domain, saving the best
        // one.
        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < qbf.size; j++) {
                if (Math.random() < 0.5)
                    qbf.variables[j] = 0.0;
                else
                    qbf.variables[j] = 1.0;
            }
            //System.out.println("x = " + Arrays.toString(qbf.variables));
            Double eval = qbf.evaluateQBF();
            //System.out.println("f(x) = " + eval);
            if (maxVal < eval)
                maxVal = eval;
        }
        System.out.println("maxVal = " + maxVal);

        // evaluates the zero array.
        for (int j = 0; j < qbf.size; j++) {
            qbf.variables[j] = 0.0;
        }
        System.out.println("x = " + Arrays.toString(qbf.variables));
        System.out.println("f(x) = " + qbf.evaluateQBF());

        // evaluates the all-ones array.
        for (int j = 0; j < qbf.size; j++) {
            qbf.variables[j] = 1.0;
        }
        System.out.println("x = " + Arrays.toString(qbf.variables));
        System.out.println("f(x) = " + qbf.evaluateQBF());

    }


}
