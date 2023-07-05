package kr.co.mayfarm.plugin.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;

import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenizerFactory;
import kr.co.mayfarm.plugin.analysis.tokenizer.KkmaTokenizer;
import kr.co.mayfarm.kkma.ma.MorphemeAnalyzer;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class KkmaTokenizerFactory extends AbstractTokenizerFactory {

    private List<String> userDicPath;

    public KkmaTokenizerFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) {
        super(indexSettings, settings, name);
        this.userDicPath = getUserDicPath(settings,env);
    }

    private List<String> getUserDicPath(Settings settings, Environment env) {
        if(settings.get("user_dictionary") != null) {
            List<String> dicPath = settings.getAsList("user_dictionary", null);
            List<String> resolvePath = new ArrayList<>();
            for(String path : dicPath) {
                Path tempPath;
                if(path.startsWith("./") || !path.startsWith("/")) {
                    if(path.startsWith("./")) {
                        path = path.substring(2);
                    }
                    tempPath = env.configFile().resolve(path);
                }
                else {
                    tempPath = Paths.get(path);
                }
                resolvePath.add(tempPath.toString());
            }
            return resolvePath;
        }
        else {
            return new ArrayList<>();
        }
    }


    @Override
    public Tokenizer create() {
        MorphemeAnalyzer morphemeAnalyzer;
        if(!userDicPath.equals("")) {
            morphemeAnalyzer = new MorphemeAnalyzer(userDicPath);
        }
        else {
            morphemeAnalyzer = new MorphemeAnalyzer();
        }

        return new KkmaTokenizer(morphemeAnalyzer);
    }
}
