package org.elasticsearch.plugin.analysis.filters;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;

public class ProductCodeTokenizerFactory extends AbstractTokenizerFactory {
    public ProductCodeTokenizerFactory(IndexSettings indexSettings, Settings settings, String name) {
        super(indexSettings, settings, name);
    }

    @Override
    public Tokenizer create() {
        //TODO : make tokenizer when i need
        return null;
    }
}
