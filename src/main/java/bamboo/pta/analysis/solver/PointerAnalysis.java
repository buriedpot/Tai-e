package bamboo.pta.analysis.solver;

import bamboo.callgraph.CallGraph;
import bamboo.pta.analysis.ProgramManager;
import bamboo.pta.analysis.context.ContextSelector;
import bamboo.pta.analysis.data.CSCallSite;
import bamboo.pta.analysis.data.CSMethod;
import bamboo.pta.analysis.data.CSVariable;
import bamboo.pta.analysis.data.DataManager;
import bamboo.pta.analysis.data.InstanceField;
import bamboo.pta.analysis.heap.HeapModel;
import bamboo.pta.set.PointsToSetFactory;

import java.util.stream.Stream;

public interface PointerAnalysis {

    void setProgramManager(ProgramManager programManager);

    void setDataManager(DataManager dataManager);

    void setContextSelector(ContextSelector contextSelector);

    void setHeapModel(HeapModel heapModel);

    void setPointsToSetFactory(PointsToSetFactory setFactory);

    void solve();

    CallGraph<CSCallSite, CSMethod> getCallGraph();

    /**
     *
     * @return all variables in the (reachable) program.
     */
    Stream<CSVariable> getVariables();

    /**
     *
     * @return all instance fields in the (reachable) program.
     */
    Stream<InstanceField> getInstanceFields();
}