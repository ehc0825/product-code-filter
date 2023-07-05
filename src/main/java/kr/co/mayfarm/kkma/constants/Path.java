package kr.co.mayfarm.kkma.constants;

import kr.co.mayfarm.plugin.analysis.plugin.KkmaAnalysisPlugin;

import java.nio.file.Paths;

public class Path {

    public static final String PLUGIN_PATH = Paths.get("").toAbsolutePath().toString()+"/plugins/"+ KkmaAnalysisPlugin.NAME;
}
