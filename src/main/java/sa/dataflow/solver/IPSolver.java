package sa.dataflow.solver;

import sa.dataflow.analysis.IPDataFlowAnalysis;
import sa.icfg.Edge;
import sa.icfg.ICFG;

import java.util.Map;

public abstract class IPSolver<Domain, Method, Node> {

    protected IPDataFlowAnalysis<Domain, Method, Node> analysis;

    protected ICFG<Method, Node> icfg;

    /**
     * In-flow value of each node.
     */
    protected Map<Node, Domain> inFlow;

    /**
     * Out-flow value of each node.
     */
    protected Map<Node, Domain> outFlow;

    /**
     * Flow value for ICFG edges.
     */
    protected Map<Edge<Node>, Domain> edgeFlow;

    protected IPSolver(IPDataFlowAnalysis<Domain, Method, Node> analysis,
                     ICFG<Method, Node> icfg) {
        this.analysis = analysis;
        this.icfg = icfg;
    }

    public void solve() {
        initialize(icfg);
        solveFixedPoint(icfg);
    }

    /**
     * Returns the data-flow value before each node.
     */
    public Map<Node, Domain> getBeforeFlow() {
        return analysis.isForward() ? inFlow : outFlow;
    }

    /**
     * Returns the data-flow value after each node.
     */
    public Map<Node, Domain> getAfterFlow() {
        return analysis.isForward() ? outFlow : inFlow;
    }

    protected void initialize(ICFG<Method, Node> icfg) {
        for (Node node : icfg) {
            if (icfg.getHeads().contains(node)) {
                inFlow.put(node, analysis.getEntryInitialFlow(node));
            }
            outFlow.put(node, analysis.newInitialFlow());
            icfg.getOutEdgesOf(node)
                    .forEach(edge ->
                            edgeFlow.put(edge, analysis.newInitialFlow()));
        }
    }

    protected abstract void solveFixedPoint(ICFG<Method, Node> icfg);
}