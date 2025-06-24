package br.ifsp.vvts.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({"br.ifsp.vvts.integration.api"})
@SuiteDisplayName("All Api tests")
@IncludeTags({"ApiTest"})
public class ApiTests {
}
