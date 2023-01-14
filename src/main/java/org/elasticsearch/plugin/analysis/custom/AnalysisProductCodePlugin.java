package org.elasticsearch.plugin.analysis.custom;

import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugin.analysis.filters.ProductCodeTokenFilterFactory;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

public class AnalysisProductCodePlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters(){
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extraTokenFilters = new HashMap<>();
        extraTokenFilters.put("product_code",ProductCodeTokenFilterFactory::new);

        return extraTokenFilters;
    }
}
