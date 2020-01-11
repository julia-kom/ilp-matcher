package bpm.profile;

import com.iise.shudi.exroru.RefinedOrderingRelation;
import com.iise.shudi.exroru.RefinedOrderingRelationsMatrix;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.List;

/**
 * Wrapper Class for BP+
 */
public class BPPlus extends AbstractProfile {
    RefinedOrderingRelationsMatrix rorm;
    NetSystem net;
    public BPPlus(NetSystem net){

        rorm = new RefinedOrderingRelationsMatrix(net);
        this.net = net;
    }


    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {


        // convert Id to row/column number in the relational matrix
        List<String> tId1 = rorm.gettId();
        int i1 = tId1.indexOf(n1.getId());
        int i2 = tId1.indexOf(n2.getId());

        // fetch base relations
        RefinedOrderingRelation causalRel = rorm.getCausalMatrix()[i1][i2];
        RefinedOrderingRelation invCausalRel = rorm.getInverseCausalMatrix()[i1][i2];
        RefinedOrderingRelation concurrentRel = rorm.getConcurrentMatrix()[i1][i2];

        // derive matrix entry from base relations
        if(causalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER) {
            if(causalRel.isAdjacency()){
                return Relation.BPP_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_INDIRECT_CAUSAL;
            }
        }else if(invCausalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(invCausalRel.isAdjacency()){
                return Relation.BPP_REVERSE_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_REVERSE_INDIRECT_CAUSAL;
            }

        }else if(concurrentRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(concurrentRel.getRelation() == com.iise.shudi.exroru.Relation.SOMETIMES){
                return Relation.BPP_SOMETIMES_CONCURRENT;
            }else{
                return Relation.BPP_ALWAYS_CONCURRENT;
            }
        }
        return Relation.BPP_CONFLICT; // todo NONE or CONFLICT  ????? CONFLICT NOT EXPLICITLY DEFINED IN CODE.
    }

    /**
     * Matching Penalty for BP+ acc to "BP+: An Improved Behavioral Profile Metric for Process Models"
     * @param r1 Relation 1
     * @param r2 Relation 2
     * @return double similarity of the two relations
     */
    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        if(r1 == r2){
            return 1.0;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_INDIRECT_CAUSAL) ||
                (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_INDIRECT_CAUSAL) ||
                (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL) ||
                (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL)){
            return 0.75;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT)){
            return 0.5;
        }else if((r1 == Relation.BPP_INDIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_INDIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT)){
            return 0.25;
        }else if((r1 == Relation.BPP_ALWAYS_CONCURRENT && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
                (r2 == Relation.BPP_ALWAYS_CONCURRENT && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
            return 0.9;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
        return 0.49;
    }else if((r1 == Relation.BPP_INDIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_INDIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
        return 0.24;
    }
        return 0.0;
    }

    @Override
    public String toString(){
        String res = "";
        for(Transition t1 : net.getTransitions()){
            for(Transition t2 : net.getTransitions()){
                res += " | " + this.getRelationForEntities(t1,t2);
            }
            res += "| \n";
        }
        return res;
    }

}
