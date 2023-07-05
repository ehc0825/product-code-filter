package kr.co.mayfarm.plugin.analysis.plugin;

import org.elasticsearch.index.analysis.CharFilterFactory;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import kr.co.mayfarm.plugin.analysis.HanCharFilterFactory;
import kr.co.mayfarm.plugin.analysis.KkmaPartOfSpeechFilterFactory;
import kr.co.mayfarm.plugin.analysis.KkmaTokenizerFactory;
import kr.co.mayfarm.plugin.analysis.charFilter.HanCharFilter;
import kr.co.mayfarm.plugin.analysis.filter.KkmaPartOfSpeechFilter;
import kr.co.mayfarm.plugin.analysis.tokenizer.KkmaTokenizer;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;


public class KkmaAnalysisPlugin extends Plugin implements AnalysisPlugin {

    public static final String NAME = "kkma-analyzer";

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String,AnalysisModule.AnalysisProvider<TokenizerFactory>> pluginMap = new HashMap<>();
        pluginMap.put(KkmaTokenizer.NAME, KkmaTokenizerFactory::new);
        return pluginMap;
    }

    @Override
    public Map<String,AnalysisModule.AnalysisProvider<CharFilterFactory>> getCharFilters(){
        Map<String,AnalysisModule.AnalysisProvider<CharFilterFactory>> pluginMap = new HashMap<>();
        pluginMap.put(HanCharFilter.NAME, HanCharFilterFactory::new);
        return pluginMap;
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters(){
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> pluginMap = new HashMap<>();
        pluginMap.put(KkmaPartOfSpeechFilter.NAME, KkmaPartOfSpeechFilterFactory::new);
        return pluginMap;
    }


}
