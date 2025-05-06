package br.ifsp.application.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeTags("TDD")
@SelectPackages("br.ifsp.application")
public class TddTestSuite {}
