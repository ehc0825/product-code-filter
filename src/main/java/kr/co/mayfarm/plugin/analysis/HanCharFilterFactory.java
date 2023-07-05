package kr.co.mayfarm.plugin.analysis;


import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractCharFilterFactory;
import kr.co.mayfarm.plugin.analysis.charFilter.HanCharFilter;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.LinkOption;

public class HanCharFilterFactory extends AbstractCharFilterFactory {
    private final String pluginPath;


    public HanCharFilterFactory(IndexSettings indexSettings, Environment environment, String name, Settings settings)
            throws IOException {
        super(indexSettings,name);
        this.pluginPath = environment.pluginsFile().toRealPath(LinkOption.NOFOLLOW_LINKS).toString();
    }

    @Override
    public Reader create(Reader reader) {
        return new HanCharFilter(reader,pluginPath);
    }
}
