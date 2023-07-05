package kr.co.mayfarm.kkma.sp;

import kr.co.mayfarm.kkma.constants.POSTag;

public class ParseGrammar {
    String relation;
    String dependantMorp;
    long dependantTag;
    String dominantMorp;
    long dominantTag;
    int distance;
    int priority;

    public ParseGrammar(String relation, long dependantTag, long dominantTag, int distance, int priority) {
        this(relation, (String)null, dependantTag, (String)null, dominantTag, distance, priority);
    }

    public ParseGrammar(String relation, long dependantTag, String dominantMorp, long dominantTag, int distance, int priority) {
        this(relation, (String)null, dependantTag, dominantMorp, dominantTag, distance, priority);
    }

    public ParseGrammar(String relation, String dependantMorp, long dependantTag, String dominantMorp, long dominantTag, int distance, int priority) {
        this.relation = null;
        this.dependantMorp = null;
        this.dependantTag = 0L;
        this.dominantMorp = null;
        this.dominantTag = 0L;
        this.distance = 1;
        this.priority = 10;
        this.relation = relation;
        this.dependantMorp = dependantMorp;
        this.dependantTag = dependantTag;
        this.dominantMorp = dominantMorp;
        this.dominantTag = dominantTag;
        this.distance = distance;
        this.priority = priority;
    }

    public ParseTreeEdge dominate(ParseTreeNode prevNode, ParseTreeNode nextNode, int distance) {
        boolean matchedPrev = false;
        boolean matchedNext = false;
        boolean isInRange = false;
        if (this.dominantTag == POSTag.VCP) {
            if (prevNode.isLastTagOf(this.dependantTag) && nextNode.containsTagOf(this.dominantTag) && distance <= this.distance) {
                return new ParseTreeEdge(this.relation, prevNode, nextNode, distance, this.priority);
            }
        } else if (prevNode.isLastMorpOf(this.dependantMorp, this.dependantTag) && nextNode.isFirstMorpOf(this.dominantMorp, this.dominantTag) && distance <= this.distance) {
            return new ParseTreeEdge(this.relation, prevNode, nextNode, distance, this.priority);
        }

        matchedPrev = prevNode.isLastMorpOf(this.dependantMorp, this.dependantTag);
        if (this.dominantTag == POSTag.VCP) {
            matchedNext = nextNode.containsTagOf(this.dominantTag);
        } else if ((this.dominantTag & (POSTag.XSV | POSTag.XSA)) > 0L) {
            matchedNext = nextNode.containsTagOf(POSTag.XSV | POSTag.XSA);
        } else {
            matchedNext = nextNode.isFirstMorpOf(this.dominantMorp, this.dominantTag);
        }

        isInRange = distance <= this.distance;
        return matchedPrev && matchedNext && isInRange ? new ParseTreeEdge(this.relation, prevNode, nextNode, distance, this.priority) : null;
    }
}
