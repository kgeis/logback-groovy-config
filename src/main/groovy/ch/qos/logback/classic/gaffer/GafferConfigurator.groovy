package ch.qos.logback.classic.gaffer

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.gaffer.ConfigurationDelegate
import ch.qos.logback.classic.gaffer.ScriptExpressionChecker
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil
import ch.qos.logback.core.status.OnConsoleStatusListener
import ch.qos.logback.core.util.ContextUtil
import ch.qos.logback.core.util.OptionHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

import static org.codehaus.groovy.syntax.Types.*

/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
class GafferConfigurator {

    LoggerContext context

    static final String DEBUG_SYSTEM_PROPERTY_KEY = "logback.debug"

    GafferConfigurator(LoggerContext context) {
        this.context = context
    }

    protected void informContextOfURLUsedForConfiguration(URL url) {
        ConfigurationWatchListUtil.setMainWatchURL(context, url)
    }

    void run(URL url) {
        informContextOfURLUsedForConfiguration(url)
        run(url.text)
    }

    void run(File file) {
        informContextOfURLUsedForConfiguration(file.toURI().toURL())
        run(file.text)
    }

    void run(String dslText) {
        Binding binding = new Binding()
        binding.setProperty("hostname", ContextUtil.localHostName)

        // Define SecureASTCustomizer to limit allowed
        // language syntax in scripts.
        final SecureASTCustomizer astCustomizer = new SecureASTCustomizer(
                methodDefinitionAllowed: false,
                closuresAllowed: true,
                packageAllowed: false,
                indirectImportCheckEnabled: true,

                importsWhitelist: [
                        'java.lang.Object',
                        'org.springframework.beans.factory.annotation.Autowired', //test removing once this is in a real enviroment because I think this is only required because of the test in grails picking up some context that requires this.
                        'java.nio.charset.Charset.forName',
                        'com.logentries.logback.LogentriesAppender',
                        'grails.util.BuildSettings',
                        'grails.util.Environment',
                        'org.slf4j.MDC',
                        'org.springframework.boot.logging.logback.ColorConverter',
                        'org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter',
                        'java.nio.charset.Charset',
                        'java.nio.charset.StandardCharsets',



                        'ch.qos.logback.core.BasicStatusManager',
                        'ch.qos.logback.core.ConsoleAppender',
                        'ch.qos.logback.core.hook.ShutdownHook',
                        'ch.qos.logback.core.hook.ShutdownHookBase',
                        'ch.qos.logback.core.hook.DelayingShutdownHook',
                        'ch.qos.logback.core.spi.PropertyContainer',
                        'ch.qos.logback.core.spi.ContextAwareBase',
                        'ch.qos.logback.core.spi.LogbackLock',
                        'ch.qos.logback.core.spi.FilterAttachableImpl',
                        'ch.qos.logback.core.spi.ContextAwareImpl',
                        'ch.qos.logback.core.spi.ScanException',
                        'ch.qos.logback.core.spi.DeferredProcessingAware',
                        'ch.qos.logback.core.spi.ContextAware',
                        'ch.qos.logback.core.spi.LifeCycle',
                        'ch.qos.logback.core.spi.FilterReply',
                        'ch.qos.logback.core.spi.PreSerializationTransformer',
                        'ch.qos.logback.core.spi.AppenderAttachable',
                        'ch.qos.logback.core.spi.CyclicBufferTracker',
                        'ch.qos.logback.core.spi.FilterAttachable',
                        'ch.qos.logback.core.spi.ComponentTracker',
                        'ch.qos.logback.core.spi.AppenderAttachableImpl',
                        'ch.qos.logback.core.spi.PropertyDefiner',
                        'ch.qos.logback.core.spi.AbstractComponentTracker',
                        'ch.qos.logback.core.property.FileExistsPropertyDefiner',
                        'ch.qos.logback.core.property.ResourceExistsPropertyDefiner',
                        'ch.qos.logback.core.CoreConstants',
                        'ch.qos.logback.core.layout.EchoLayout',
                        'ch.qos.logback.core.Appender',
                        'ch.qos.logback.core.joran.JoranConfiguratorBase',
                        'ch.qos.logback.core.joran.spi.ActionException',
                        'ch.qos.logback.core.joran.spi.HostClassAndPropertyDouble',
                        'ch.qos.logback.core.joran.spi.JoranException',
                        'ch.qos.logback.core.joran.spi.NoAutoStart',
                        'ch.qos.logback.core.joran.spi.EventPlayer',
                        'ch.qos.logback.core.joran.spi.XMLUtil',
                        'ch.qos.logback.core.joran.spi.ConsoleTarget',
                        'ch.qos.logback.core.joran.spi.Interpreter',
                        'ch.qos.logback.core.joran.spi.SimpleRuleStore',
                        'ch.qos.logback.core.joran.spi.InterpretationContext',
                        'ch.qos.logback.core.joran.spi.RuleStore',
                        'ch.qos.logback.core.joran.spi.NoAutoStartUtil',
                        'ch.qos.logback.core.joran.spi.ElementSelector',
                        'ch.qos.logback.core.joran.spi.ConfigurationWatchList',
                        'ch.qos.logback.core.joran.spi.DefaultNestedComponentRegistry',
                        'ch.qos.logback.core.joran.spi.DefaultClass',
                        'ch.qos.logback.core.joran.spi.ElementPath',
                        'ch.qos.logback.core.joran.conditional.ThenOrElseActionBase',
                        'ch.qos.logback.core.joran.conditional.Condition',
                        'ch.qos.logback.core.joran.conditional.PropertyWrapperForScripts',
                        'ch.qos.logback.core.joran.conditional.ThenAction',
                        'ch.qos.logback.core.joran.conditional.PropertyEvalScriptBuilder',
                        'ch.qos.logback.core.joran.conditional.ElseAction',
                        'ch.qos.logback.core.joran.conditional.IfAction',
                        'ch.qos.logback.core.joran.util.beans.BeanDescriptionCache',
                        'ch.qos.logback.core.joran.util.beans.BeanDescriptionFactory',
                        'ch.qos.logback.core.joran.util.beans.BeanDescription',
                        'ch.qos.logback.core.joran.util.beans.BeanUtil',
                        'ch.qos.logback.core.joran.util.PropertySetter',
                        'ch.qos.logback.core.joran.util.ConfigurationWatchListUtil',
                        'ch.qos.logback.core.joran.util.StringToObjectConverter',
                        'ch.qos.logback.core.joran.GenericConfigurator',
                        'ch.qos.logback.core.joran.action.ImplicitAction',
                        'ch.qos.logback.core.joran.action.IncludeAction',
                        'ch.qos.logback.core.joran.action.NOPAction',
                        'ch.qos.logback.core.joran.action.IADataForBasicProperty',
                        'ch.qos.logback.core.joran.action.TimestampAction',
                        'ch.qos.logback.core.joran.action.AbstractEventEvaluatorAction',
                        'ch.qos.logback.core.joran.action.ParamAction',
                        'ch.qos.logback.core.joran.action.AppenderAction',
                        'ch.qos.logback.core.joran.action.DefinePropertyAction',
                        'ch.qos.logback.core.joran.action.StatusListenerAction',
                        'ch.qos.logback.core.joran.action.ContextPropertyAction',
                        'ch.qos.logback.core.joran.action.NestedComplexPropertyIA',
                        'ch.qos.logback.core.joran.action.NestedBasicPropertyIA',
                        'ch.qos.logback.core.joran.action.Action',
                        'ch.qos.logback.core.joran.action.AppenderRefAction',
                        'ch.qos.logback.core.joran.action.ActionUtil',
                        'ch.qos.logback.core.joran.action.ShutdownHookAction',
                        'ch.qos.logback.core.joran.action.IADataForComplexProperty',
                        'ch.qos.logback.core.joran.action.ConversionRuleAction',
                        'ch.qos.logback.core.joran.action.ActionConst',
                        'ch.qos.logback.core.joran.action.PropertyAction',
                        'ch.qos.logback.core.joran.action.NewRuleAction',
                        'ch.qos.logback.core.joran.node.ComponentNode',
                        'ch.qos.logback.core.joran.event.EndEvent',
                        'ch.qos.logback.core.joran.event.SaxEventRecorder',
                        'ch.qos.logback.core.joran.event.SaxEvent',
                        'ch.qos.logback.core.joran.event.BodyEvent',
                        'ch.qos.logback.core.joran.event.StartEvent',
                        'ch.qos.logback.core.joran.event.InPlayListener',
                        'ch.qos.logback.core.joran.event.stax.EndEvent',
                        'ch.qos.logback.core.joran.event.stax.StaxEventRecorder',
                        'ch.qos.logback.core.joran.event.stax.BodyEvent',
                        'ch.qos.logback.core.joran.event.stax.StartEvent',
                        'ch.qos.logback.core.joran.event.stax.StaxEvent',
                        'ch.qos.logback.core.LogbackException',
                        'ch.qos.logback.core.PropertyDefinerBase',
                        'ch.qos.logback.core.helpers.CyclicBuffer',
                        'ch.qos.logback.core.helpers.ThrowableToStringArray',
                        'ch.qos.logback.core.helpers.Transform',
                        'ch.qos.logback.core.helpers.NOPAppender',
                        'ch.qos.logback.core.net.LoginAuthenticator',
                        'ch.qos.logback.core.net.DefaultSocketConnector',
                        'ch.qos.logback.core.net.ssl.KeyStoreFactoryBean',
                        'ch.qos.logback.core.net.ssl.SSLParametersConfiguration',
                        'ch.qos.logback.core.net.ssl.SSLComponent',
                        'ch.qos.logback.core.net.ssl.SSLNestedComponentRegistryRules',
                        'ch.qos.logback.core.net.ssl.SSLConfigurableSocket',
                        'ch.qos.logback.core.net.ssl.SSLConfigurableServerSocket',
                        'ch.qos.logback.core.net.ssl.SSLConfiguration',
                        'ch.qos.logback.core.net.ssl.ConfigurableSSLSocketFactory',
                        'ch.qos.logback.core.net.ssl.ConfigurableSSLServerSocketFactory',
                        'ch.qos.logback.core.net.ssl.SecureRandomFactoryBean',
                        'ch.qos.logback.core.net.ssl.SSLContextFactoryBean',
                        'ch.qos.logback.core.net.ssl.SSL',
                        'ch.qos.logback.core.net.ssl.SSLConfigurable',
                        'ch.qos.logback.core.net.ssl.TrustManagerFactoryFactoryBean',
                        'ch.qos.logback.core.net.ssl.KeyManagerFactoryFactoryBean',
                        'ch.qos.logback.core.net.SMTPAppenderBase',
                        'ch.qos.logback.core.net.SyslogAppenderBase',
                        'ch.qos.logback.core.net.SocketConnector',
                        'ch.qos.logback.core.net.SyslogOutputStream',
                        'ch.qos.logback.core.net.QueueFactory',
                        'ch.qos.logback.core.net.HardenedObjectInputStream',
                        'ch.qos.logback.core.net.AbstractSocketAppender',
                        'ch.qos.logback.core.net.AbstractSSLSocketAppender',
                        'ch.qos.logback.core.net.ObjectWriterFactory',
                        'ch.qos.logback.core.net.ObjectWriter',
                        'ch.qos.logback.core.net.AutoFlushingObjectWriter',
                        'ch.qos.logback.core.net.SyslogConstants',
                        'ch.qos.logback.core.net.server.ServerRunner',
                        'ch.qos.logback.core.net.server.Client',
                        'ch.qos.logback.core.net.server.ServerListener',
                        'ch.qos.logback.core.net.server.RemoteReceiverStreamClient',
                        'ch.qos.logback.core.net.server.AbstractServerSocketAppender',
                        'ch.qos.logback.core.net.server.ClientVisitor',
                        'ch.qos.logback.core.net.server.RemoteReceiverClient',
                        'ch.qos.logback.core.net.server.RemoteReceiverServerRunner',
                        'ch.qos.logback.core.net.server.SSLServerSocketAppenderBase',
                        'ch.qos.logback.core.net.server.ConcurrentServerRunner',
                        'ch.qos.logback.core.net.server.ServerSocketListener',
                        'ch.qos.logback.core.net.server.RemoteReceiverServerListener',
                        'ch.qos.logback.core.UnsynchronizedAppenderBase',
                        'ch.qos.logback.core.AsyncAppenderBase',
                        'ch.qos.logback.core.util.CloseUtil',
                        'ch.qos.logback.core.util.DatePatternToRegexUtil',
                        'ch.qos.logback.core.util.StatusListenerConfigHelper',
                        'ch.qos.logback.core.util.SystemInfo',
                        'ch.qos.logback.core.util.DefaultInvocationGate',
                        'ch.qos.logback.core.util.CachingDateFormatter',
                        'ch.qos.logback.core.util.InterruptUtil',
                        'ch.qos.logback.core.util.LocationUtil',
                        'ch.qos.logback.core.util.TimeUtil',
                        'ch.qos.logback.core.util.COWArrayList',
                        'ch.qos.logback.core.util.Loader',
                        'ch.qos.logback.core.util.CharSequenceState',
                        'ch.qos.logback.core.util.StatusPrinter',
                        'ch.qos.logback.core.util.Duration',
                        'ch.qos.logback.core.util.ContentTypeUtil',
                        'ch.qos.logback.core.util.FileUtil',
                        'ch.qos.logback.core.util.DynamicClassLoadingException',
                        'ch.qos.logback.core.util.InvocationGate',
                        'ch.qos.logback.core.util.OptionHelper',
                        'ch.qos.logback.core.util.IncompatibleClassException',
                        'ch.qos.logback.core.util.ExecutorServiceUtil',
                        'ch.qos.logback.core.util.StringCollectionUtil',
                        'ch.qos.logback.core.util.CharSequenceToRegexMapper',
                        'ch.qos.logback.core.util.FixedDelay',
                        'ch.qos.logback.core.util.FileSize',
                        'ch.qos.logback.core.util.DelayStrategy',
                        'ch.qos.logback.core.util.EnvUtil',
                        'ch.qos.logback.core.util.ContextUtil',
                        'ch.qos.logback.core.util.AggregationType',
                        'ch.qos.logback.core.util.PropertySetterException',
                        'ch.qos.logback.core.LifeCycleManager',
                        'ch.qos.logback.core.LayoutBase',
                        'ch.qos.logback.core.encoder.NonClosableInputStream',
                        'ch.qos.logback.core.encoder.Encoder',
                        'ch.qos.logback.core.encoder.ByteArrayUtil',
                        'ch.qos.logback.core.encoder.EncoderBase',
                        'ch.qos.logback.core.encoder.EchoEncoder',
                        'ch.qos.logback.core.encoder.LayoutWrappingEncoder',
                        'ch.qos.logback.core.recovery.RecoveryCoordinator',
                        'ch.qos.logback.core.recovery.ResilientOutputStreamBase',
                        'ch.qos.logback.core.recovery.ResilientSyslogOutputStream',
                        'ch.qos.logback.core.recovery.ResilientFileOutputStream',
                        'ch.qos.logback.core.AppenderBase',
                        'ch.qos.logback.core.subst.Node',
                        'ch.qos.logback.core.subst.Parser',
                        'ch.qos.logback.core.subst.Token',
                        'ch.qos.logback.core.subst.NodeToStringTransformer',
                        'ch.qos.logback.core.subst.Tokenizer',
                        'ch.qos.logback.core.FileAppender',
                        'ch.qos.logback.core.sift.AppenderFactory',
                        'ch.qos.logback.core.sift.SiftingAppenderBase',
                        'ch.qos.logback.core.sift.SiftingJoranConfiguratorBase',
                        'ch.qos.logback.core.sift.AbstractDiscriminator',
                        'ch.qos.logback.core.sift.Discriminator',
                        'ch.qos.logback.core.sift.AbstractAppenderFactoryUsingJoran',
                        'ch.qos.logback.core.sift.AppenderTracker',
                        'ch.qos.logback.core.sift.DefaultDiscriminator',
                        'ch.qos.logback.core.html.CssBuilder',
                        'ch.qos.logback.core.html.NOPThrowableRenderer',
                        'ch.qos.logback.core.html.HTMLLayoutBase',
                        'ch.qos.logback.core.html.IThrowableRenderer',
                        'ch.qos.logback.core.rolling.TriggeringPolicyBase',
                        'ch.qos.logback.core.rolling.helper.Compressor',
                        'ch.qos.logback.core.rolling.helper.PeriodicityType',
                        'ch.qos.logback.core.rolling.helper.TokenConverter',
                        'ch.qos.logback.core.rolling.helper.IntegerTokenConverter',
                        'ch.qos.logback.core.rolling.helper.CompressionMode',
                        'ch.qos.logback.core.rolling.helper.ArchiveRemover',
                        'ch.qos.logback.core.rolling.helper.FileFilterUtil',
                        'ch.qos.logback.core.rolling.helper.RenameUtil',
                        'ch.qos.logback.core.rolling.helper.DateTokenConverter',
                        'ch.qos.logback.core.rolling.helper.FileNamePattern',
                        'ch.qos.logback.core.rolling.helper.RollingCalendar',
                        'ch.qos.logback.core.rolling.helper.FileStoreUtil',
                        'ch.qos.logback.core.rolling.helper.SizeAndTimeBasedArchiveRemover',
                        'ch.qos.logback.core.rolling.helper.TimeBasedArchiveRemover',
                        'ch.qos.logback.core.rolling.helper.MonoTypedConverter',
                        'ch.qos.logback.core.rolling.RollingPolicyBase',
                        'ch.qos.logback.core.rolling.RollingFileAppender',
                        'ch.qos.logback.core.rolling.FixedWindowRollingPolicy',
                        'ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicyBase',
                        'ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy',
                        'ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy',
                        'ch.qos.logback.core.rolling.RollingPolicy',
                        'ch.qos.logback.core.rolling.TimeBasedRollingPolicy',
                        'ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy',
                        'ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy',
                        'ch.qos.logback.core.rolling.RolloverFailure',
                        'ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP',
                        'ch.qos.logback.core.rolling.TriggeringPolicy',
                        'ch.qos.logback.core.pattern.ReplacingCompositeConverter',
                        'ch.qos.logback.core.pattern.ConverterUtil',
                        'ch.qos.logback.core.pattern.parser.Compiler',
                        'ch.qos.logback.core.pattern.parser.Node',
                        'ch.qos.logback.core.pattern.parser.Parser',
                        'ch.qos.logback.core.pattern.parser.Token',
                        'ch.qos.logback.core.pattern.parser.OptionTokenizer',
                        'ch.qos.logback.core.pattern.parser.TokenStream',
                        'ch.qos.logback.core.pattern.parser.CompositeNode',
                        'ch.qos.logback.core.pattern.parser.FormattingNode',
                        'ch.qos.logback.core.pattern.parser.SimpleKeywordNode',
                        'ch.qos.logback.core.pattern.Converter',
                        'ch.qos.logback.core.pattern.PatternLayoutEncoderBase',
                        'ch.qos.logback.core.pattern.LiteralConverter',
                        'ch.qos.logback.core.pattern.PostCompileProcessor',
                        'ch.qos.logback.core.pattern.util.RegularEscapeUtil',
                        'ch.qos.logback.core.pattern.util.AsIsEscapeUtil',
                        'ch.qos.logback.core.pattern.util.AlmostAsIsEscapeUtil',
                        'ch.qos.logback.core.pattern.util.IEscapeUtil',
                        'ch.qos.logback.core.pattern.util.RestrictedEscapeUtil',
                        'ch.qos.logback.core.pattern.SpacePadder',
                        'ch.qos.logback.core.pattern.CompositeConverter',
                        'ch.qos.logback.core.pattern.PatternLayoutBase',
                        'ch.qos.logback.core.pattern.DynamicConverter',
                        'ch.qos.logback.core.pattern.color.YellowCompositeConverter',
                        'ch.qos.logback.core.pattern.color.ANSIConstants',
                        'ch.qos.logback.core.pattern.color.BoldYellowCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldBlueCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldWhiteCompositeConverter',
                        'ch.qos.logback.core.pattern.color.CyanCompositeConverter',
                        'ch.qos.logback.core.pattern.color.MagentaCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BlueCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BlackCompositeConverter',
                        'ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase',
                        'ch.qos.logback.core.pattern.color.GrayCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldMagentaCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldCyanCompositeConverter',
                        'ch.qos.logback.core.pattern.color.RedCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldGreenCompositeConverter',
                        'ch.qos.logback.core.pattern.color.BoldRedCompositeConverter',
                        'ch.qos.logback.core.pattern.color.GreenCompositeConverter',
                        'ch.qos.logback.core.pattern.color.WhiteCompositeConverter',
                        'ch.qos.logback.core.pattern.FormattingConverter',
                        'ch.qos.logback.core.pattern.IdentityCompositeConverter',
                        'ch.qos.logback.core.pattern.FormatInfo',
                        'ch.qos.logback.core.OutputStreamAppender',
                        'ch.qos.logback.core.boolex.JaninoEventEvaluatorBase',
                        'ch.qos.logback.core.boolex.Matcher',
                        'ch.qos.logback.core.boolex.EventEvaluatorBase',
                        'ch.qos.logback.core.boolex.EvaluationException',
                        'ch.qos.logback.core.boolex.EventEvaluator',
                        'ch.qos.logback.core.read.CyclicBufferAppender',
                        'ch.qos.logback.core.read.ListAppender',
                        'ch.qos.logback.core.Context',
                        'ch.qos.logback.core.ContextBase',
                        'ch.qos.logback.core.status.StatusListenerAsList',
                        'ch.qos.logback.core.status.StatusBase',
                        'ch.qos.logback.core.status.NopStatusListener',
                        'ch.qos.logback.core.status.StatusUtil',
                        'ch.qos.logback.core.status.OnPrintStreamStatusListenerBase',
                        'ch.qos.logback.core.status.StatusManager',
                        'ch.qos.logback.core.status.ViewStatusMessagesServletBase',
                        'ch.qos.logback.core.status.ErrorStatus',
                        'ch.qos.logback.core.status.Status',
                        'ch.qos.logback.core.status.StatusListener',
                        'ch.qos.logback.core.status.InfoStatus',
                        'ch.qos.logback.core.status.OnConsoleStatusListener',
                        'ch.qos.logback.core.status.WarnStatus',
                        'ch.qos.logback.core.status.OnErrorConsoleStatusListener',
                        'ch.qos.logback.core.filter.EvaluatorFilter',
                        'ch.qos.logback.core.filter.Filter',
                        'ch.qos.logback.core.filter.AbstractMatcherFilter',
                        'ch.qos.logback.core.Layout',



                        'ch.qos.logback.classic.jmx.JMXConfigurator',
                        'ch.qos.logback.classic.jmx.MBeanUtil',
                        'ch.qos.logback.classic.jmx.JMXConfiguratorMBean',
                        'ch.qos.logback.classic.BasicConfigurator',
                        'ch.qos.logback.classic.spi.ThrowableProxy',
                        'ch.qos.logback.classic.spi.ThrowableProxyVO',
                        'ch.qos.logback.classic.spi.ThrowableProxyUtil',
                        'ch.qos.logback.classic.spi.LoggerContextVO',
                        'ch.qos.logback.classic.spi.TurboFilterList',
                        'ch.qos.logback.classic.spi.LoggingEvent',
                        'ch.qos.logback.classic.spi.LoggerRemoteView',
                        'ch.qos.logback.classic.spi.LoggerComparator',
                        'ch.qos.logback.classic.spi.LoggerContextAware',
                        'ch.qos.logback.classic.spi.PlatformInfo',
                        'ch.qos.logback.classic.spi.Configurator',
                        'ch.qos.logback.classic.spi.LoggerContextListener',
                        'ch.qos.logback.classic.spi.LoggerContextAwareBase',
                        'ch.qos.logback.classic.spi.ClassPackagingData',
                        'ch.qos.logback.classic.spi.ILoggingEvent',
                        'ch.qos.logback.classic.spi.StackTraceElementProxy',
                        'ch.qos.logback.classic.spi.PackagingDataCalculator',
                        'ch.qos.logback.classic.spi.IThrowableProxy',
                        'ch.qos.logback.classic.spi.LoggingEventVO',
                        'ch.qos.logback.classic.spi.CallerData',
                        'ch.qos.logback.classic.spi.STEUtil',
                        'ch.qos.logback.classic.spi.EventArgUtil',
                        'ch.qos.logback.classic.servlet.LogbackServletContainerInitializer',
                        'ch.qos.logback.classic.servlet.LogbackServletContextListener',
                        'ch.qos.logback.classic.ViewStatusMessagesServlet',
                        'ch.qos.logback.classic.ClassicConstants',
                        'ch.qos.logback.classic.layout.TTLLLayout',
                        'ch.qos.logback.classic.joran.ReconfigureOnChangeTaskListener',
                        'ch.qos.logback.classic.joran.ReconfigureOnChangeTask',
                        'ch.qos.logback.classic.joran.action.ContextNameAction',
                        'ch.qos.logback.classic.joran.action.ConsolePluginAction',
                        'ch.qos.logback.classic.joran.action.EvaluatorAction',
                        'ch.qos.logback.classic.joran.action.LoggerAction',
                        'ch.qos.logback.classic.joran.action.LevelAction',
                        'ch.qos.logback.classic.joran.action.LoggerContextListenerAction',
                        'ch.qos.logback.classic.joran.action.InsertFromJNDIAction',
                        'ch.qos.logback.classic.joran.action.ReceiverAction',
                        'ch.qos.logback.classic.joran.action.RootLoggerAction',
                        'ch.qos.logback.classic.joran.action.JMXConfiguratorAction',
                        'ch.qos.logback.classic.joran.action.ConfigurationAction',
                        'ch.qos.logback.classic.joran.JoranConfigurator',
                        'ch.qos.logback.classic.helpers.MDCInsertingServletFilter',
                        'ch.qos.logback.classic.Level',
                        'ch.qos.logback.classic.net.SSLSocketReceiver',
                        'ch.qos.logback.classic.net.ReceiverBase',
                        'ch.qos.logback.classic.net.SimpleSocketServer',
                        'ch.qos.logback.classic.net.SimpleSSLSocketServer',
                        'ch.qos.logback.classic.net.SocketNode',
                        'ch.qos.logback.classic.net.SMTPAppender',
                        'ch.qos.logback.classic.net.SocketReceiver',
                        'ch.qos.logback.classic.net.SocketAcceptor',
                        'ch.qos.logback.classic.net.SSLSocketAppender',
                        'ch.qos.logback.classic.net.LoggingEventPreSerializationTransformer',
                        'ch.qos.logback.classic.net.server.RemoteAppenderStreamClient',
                        'ch.qos.logback.classic.net.server.RemoteAppenderServerListener',
                        'ch.qos.logback.classic.net.server.SSLServerSocketAppender',
                        'ch.qos.logback.classic.net.server.RemoteAppenderClient',
                        'ch.qos.logback.classic.net.server.HardenedLoggingEventInputStream',
                        'ch.qos.logback.classic.net.server.ServerSocketAppender',
                        'ch.qos.logback.classic.net.server.SSLServerSocketReceiver',
                        'ch.qos.logback.classic.net.server.RemoteAppenderServerRunner',
                        'ch.qos.logback.classic.net.server.ServerSocketReceiver',
                        'ch.qos.logback.classic.net.SocketAppender',
                        'ch.qos.logback.classic.net.SyslogAppender',
                        'ch.qos.logback.classic.PatternLayout',
                        'ch.qos.logback.classic.util.ContextSelectorStaticBinder',
                        'ch.qos.logback.classic.util.StatusViaSLF4JLoggerFactory',
                        'ch.qos.logback.classic.util.JNDIUtil',
                        'ch.qos.logback.classic.util.LevelToSyslogSeverity',
                        'ch.qos.logback.classic.util.LoggerNameUtil',
                        'ch.qos.logback.classic.util.LogbackMDCAdapter',
                        'ch.qos.logback.classic.util.CopyOnInheritThreadLocal',
                        'ch.qos.logback.classic.util.ContextInitializer',
                        'ch.qos.logback.classic.util.EnvUtil',
                        'ch.qos.logback.classic.util.DefaultNestedComponentRules',
                        'ch.qos.logback.classic.AsyncAppender',
                        'ch.qos.logback.classic.jul.JULHelper',
                        'ch.qos.logback.classic.jul.LevelChangePropagator',
                        'ch.qos.logback.classic.encoder.PatternLayoutEncoder',
                        'ch.qos.logback.classic.db.names.DBNameResolver',
                        'ch.qos.logback.classic.db.names.ColumnName',
                        'ch.qos.logback.classic.db.names.TableName',
                        'ch.qos.logback.classic.db.names.DefaultDBNameResolver',
                        'ch.qos.logback.classic.db.names.SimpleDBNameResolver',
                        'ch.qos.logback.classic.log4j.XMLLayout',
                        'ch.qos.logback.classic.LoggerContext',
                        'ch.qos.logback.classic.turbo.TurboFilter',
                        'ch.qos.logback.classic.turbo.MDCFilter',
                        'ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter',
                        'ch.qos.logback.classic.turbo.DuplicateMessageFilter',
                        'ch.qos.logback.classic.turbo.MarkerFilter',
                        'ch.qos.logback.classic.turbo.MDCValueLevelPair',
                        'ch.qos.logback.classic.turbo.DynamicThresholdFilter',
                        'ch.qos.logback.classic.turbo.MatchingFilter',
                        'ch.qos.logback.classic.turbo.LRUMessageCache',
                        'ch.qos.logback.classic.selector.servlet.LoggerContextFilter',
                        'ch.qos.logback.classic.selector.servlet.ContextDetachingSCL',
                        'ch.qos.logback.classic.selector.ContextJNDISelector',
                        'ch.qos.logback.classic.selector.DefaultContextSelector',
                        'ch.qos.logback.classic.selector.ContextSelector',
                        'ch.qos.logback.classic.sift.MDCBasedDiscriminator',
                        'ch.qos.logback.classic.sift.SiftingJoranConfigurator',
                        'ch.qos.logback.classic.sift.JNDIBasedContextDiscriminator',
                        'ch.qos.logback.classic.sift.AppenderFactoryUsingJoran',
                        'ch.qos.logback.classic.sift.ContextBasedDiscriminator',
                        'ch.qos.logback.classic.sift.SiftingAppender',
                        'ch.qos.logback.classic.sift.SiftAction',
                        'ch.qos.logback.classic.html.UrlCssBuilder',
                        'ch.qos.logback.classic.html.HTMLLayout',
                        'ch.qos.logback.classic.html.DefaultCssBuilder',
                        'ch.qos.logback.classic.html.DefaultThrowableRenderer',
                        'ch.qos.logback.classic.Logger',
                        'ch.qos.logback.classic.pattern.ThrowableHandlingConverter',
                        'ch.qos.logback.classic.pattern.ContextNameConverter',
                        'ch.qos.logback.classic.pattern.LocalSequenceNumberConverter',
                        'ch.qos.logback.classic.pattern.ClassOfCallerConverter',
                        'ch.qos.logback.classic.pattern.PrefixCompositeConverter',
                        'ch.qos.logback.classic.pattern.LineOfCallerConverter',
                        'ch.qos.logback.classic.pattern.EnsureExceptionHandling',
                        'ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator',
                        'ch.qos.logback.classic.pattern.FileOfCallerConverter',
                        'ch.qos.logback.classic.pattern.LevelConverter',
                        'ch.qos.logback.classic.pattern.ExtendedThrowableProxyConverter',
                        'ch.qos.logback.classic.pattern.NamedConverter',
                        'ch.qos.logback.classic.pattern.ClassicConverter',
                        'ch.qos.logback.classic.pattern.NopThrowableInformationConverter',
                        'ch.qos.logback.classic.pattern.RootCauseFirstThrowableProxyConverter',
                        'ch.qos.logback.classic.pattern.MethodOfCallerConverter',
                        'ch.qos.logback.classic.pattern.CallerDataConverter',
                        'ch.qos.logback.classic.pattern.ClassNameOnlyAbbreviator',
                        'ch.qos.logback.classic.pattern.MarkerConverter',
                        'ch.qos.logback.classic.pattern.RelativeTimeConverter',
                        'ch.qos.logback.classic.pattern.DateConverter',
                        'ch.qos.logback.classic.pattern.PropertyConverter',
                        'ch.qos.logback.classic.pattern.ThreadConverter',
                        'ch.qos.logback.classic.pattern.LineSeparatorConverter',
                        'ch.qos.logback.classic.pattern.MDCConverter',
                        'ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter',
                        'ch.qos.logback.classic.pattern.ThrowableProxyConverter',
                        'ch.qos.logback.classic.pattern.Abbreviator',
                        'ch.qos.logback.classic.pattern.Util',
                        'ch.qos.logback.classic.pattern.LoggerConverter',
                        'ch.qos.logback.classic.pattern.SyslogStartConverter',
                        'ch.qos.logback.classic.pattern.MessageConverter',
                        'ch.qos.logback.classic.gaffer.GafferUtil',
                        'ch.qos.logback.classic.boolex.OnMarkerEvaluator',
                        'ch.qos.logback.classic.boolex.JaninoEventEvaluator',
                        'ch.qos.logback.classic.boolex.OnErrorEvaluator',
                        'ch.qos.logback.classic.boolex.GEventEvaluator',
                        'ch.qos.logback.classic.boolex.IEvaluator',
                        'ch.qos.logback.classic.filter.ThresholdFilter',
                        'ch.qos.logback.classic.filter.LevelFilter'


                ],

                staticImportsWhitelist: [
                        'java.nio.charset.Charset.forName',
                        'java.lang.Object.conversionRule',
                        'java.lang.Object.appender',
                        'java.lang.Object.encoder',
                        'java.lang.Object.logger',
                        'java.lang.Object.root'
                ],


                // Only allow plus and minus tokens.
                tokensWhitelist: [
                        DIVIDE, PLUS, MINUS,
                        MULTIPLY, MOD, POWER,
                        PLUS_PLUS, MINUS_MINUS,
                        PLUS_EQUAL, LOGICAL_AND, COMPARE_EQUAL,
                        COMPARE_NOT_EQUAL, COMPARE_LESS_THAN, COMPARE_LESS_THAN_EQUAL,
                        LOGICAL_OR, NOT, COMPARE_GREATER_THAN, COMPARE_GREATER_THAN_EQUAL,
                        EQUALS, COMPARE_NOT_EQUAL, COMPARE_EQUAL
                ],


                // Disallow constant types.
                constantTypesClassesWhiteList: [
                        Object,
                        Integer,
                        Float,
                        Long,
                        Double,
                        BigDecimal,
                        String,
                        Integer.TYPE,
                        Long.TYPE,
                        Float.TYPE,
                        Double.TYPE,
                        Boolean.TYPE
                ]
        )

        astCustomizer.addExpressionCheckers(new ScriptExpressionChecker())

        def configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers(importCustomizer(), astCustomizer)

        String debugAttrib = System.getProperty(DEBUG_SYSTEM_PROPERTY_KEY)

        if (OptionHelper.isEmpty(debugAttrib) || debugAttrib.equalsIgnoreCase("false") || debugAttrib.equalsIgnoreCase("null")) {
            // For now, Groovy/Gaffer configuration DSL does not support "debug" attribute. But in order to keep
            // the conditional logic identical to that in XML/Joran, we have this empty block.
        } else {
            OnConsoleStatusListener.addNewInstanceToContext(context)
        }

        // caller data should take into account groovy frames
        new ContextUtil(context).addGroovyPackages(context.getFrameworkPackages())

        Script dslScript = new GroovyShell(binding, configuration).parse(dslText)

        dslScript.metaClass.mixin(ConfigurationDelegate)
        dslScript.setContext(context)
        dslScript.metaClass.getDeclaredOrigin = { dslScript }

        dslScript.run()
    }

    protected ImportCustomizer importCustomizer() {
        def customizer = new ImportCustomizer()


        def core = 'ch.qos.logback.core'
        customizer.addStarImports(core, "${core}.encoder", "${core}.read", "${core}.rolling", "${core}.status", "ch.qos.logback.classic.net")

        customizer.addImports(PatternLayoutEncoder.class.name)

        customizer.addStaticStars(Level.class.name)

        customizer.addStaticImport('off', Level.class.name, 'OFF')
        customizer.addStaticImport('error', Level.class.name, 'ERROR')
        customizer.addStaticImport('warn', Level.class.name, 'WARN')
        customizer.addStaticImport('info', Level.class.name, 'INFO')
        customizer.addStaticImport('debug', Level.class.name, 'DEBUG')
        customizer.addStaticImport('trace', Level.class.name, 'TRACE')
        customizer.addStaticImport('all', Level.class.name, 'ALL')

        customizer
    }

}