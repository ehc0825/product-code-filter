package kr.co.mayfarm.plugin.analysis;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.analysis.Analysis;
import kr.co.mayfarm.plugin.analysis.enums.PosType;
import kr.co.mayfarm.plugin.analysis.filter.KkmaPartOfSpeechFilter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KkmaPartOfSpeechFilterFactory extends AbstractTokenFilterFactory {

    private Set<PosType> stopTagSet;

    private Set<PosType> defaultStopTagSet = defaultStopTag();

    public KkmaPartOfSpeechFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings) {
        super(indexSettings,name,settings);
        List<String> inputTagList = Analysis.getWordList(environment,settings,"stoptags");
        if(null == inputTagList) {
            this.stopTagSet = defaultStopTagSet;
        }
        else {
            this.stopTagSet = toStopTagSet(inputTagList);
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new KkmaPartOfSpeechFilter(tokenStream,stopTagSet);
    }

    private Set<PosType> toStopTagSet(List<String> inputTagList) {
        Set<PosType> resolveStopTag = new HashSet<>();
        for(String tag : inputTagList) {
            PosType posType = PosType.find(tag);
            if (!posType.equals(PosType.NO_SUCH_TYPE)) {
                resolveStopTag.add(posType);
            }
        }
        return resolveStopTag;
    }

    private Set<PosType> defaultStopTag() {
        HashSet<PosType> stopSet = new HashSet<>();
        stopSet.add(PosType.SW);
        stopSet.add(PosType.SO);
        stopSet.add(PosType.SE);
        stopSet.add(PosType.SS);
        stopSet.add(PosType.SP);
        stopSet.add(PosType.SF);
        stopSet.add(PosType.XSA);
        stopSet.add(PosType.XSV);
        stopSet.add(PosType.XSN);
        stopSet.add(PosType.XPV);
        stopSet.add(PosType.XPN);
        return stopSet;
    }
}
