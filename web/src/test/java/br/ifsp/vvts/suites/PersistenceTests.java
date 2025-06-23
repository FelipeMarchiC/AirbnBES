package br.ifsp.vvts.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({"br.ifsp.vvts.integration.persistence"})
@SuiteDisplayName("All persistence tests")
@IncludeTags({"PersistenceTest"})
public class PersistenceTests {
}
