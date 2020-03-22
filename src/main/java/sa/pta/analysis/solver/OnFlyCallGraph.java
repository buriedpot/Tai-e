package sa.pta.analysis.solver;

import sa.callgraph.AbstractCallGraph;
import sa.callgraph.Edge;
import sa.pta.analysis.context.Context;
import sa.pta.analysis.data.CSCallSite;
import sa.pta.analysis.data.CSMethod;
import sa.pta.analysis.data.DataManager;
import sa.pta.element.CallSite;
import sa.pta.element.Method;
import sa.pta.statement.Call;
import sa.pta.statement.Statement;
import sa.util.CollectionUtils;

class OnFlyCallGraph extends AbstractCallGraph<CSCallSite, CSMethod> {

    private DataManager dataManager;

    OnFlyCallGraph(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    boolean addEdge(Edge<CSCallSite, CSMethod> edge) {
        return CollectionUtils.addToMapSet(callSiteToEdges, edge.getCallSite(), edge) ||
                CollectionUtils.addToMapSet(calleeToEdges, edge.getCallee(), edge);
    }

    boolean containsEdge(Edge<CSCallSite, CSMethod> edge) {
        return getEdgesOf(edge.getCallSite()).contains(edge);
    }

    @Override
    protected boolean addNewMethod(CSMethod csMethod) {
        if (reachableMethods.add(csMethod)) {
            Method method = csMethod.getMethod();
            Context context = csMethod.getContext();
            for (Statement s : method.getStatements()) {
                if (s instanceof Call) {
                    CallSite callSite = ((Call) s).getCallSite();
                    CSCallSite csCallSite = dataManager
                            .getCSCallSite(context, callSite);
                    callSiteToContainer.put(csCallSite, csMethod);
                    CollectionUtils.addToMapSet(callSitesIn, csMethod, csCallSite);
                }
            }
            return true;
        } else {
            return false;
        }
    }
}