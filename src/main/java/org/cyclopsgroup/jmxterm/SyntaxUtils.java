package org.cyclopsgroup.jmxterm;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.ArrayConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.cyclopsgroup.jmxterm.utils.ValueFormat;

import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Pattern;

/**
 * Utility class for syntax checking
 *
 * @author <a href="mailto:jiaqi.guo@gmail.com">Jiaqi Guo</a>
 */
public final class SyntaxUtils {
  /**
   * NULL string identifier
   */
  public static final String NULL = ValueFormat.NULL;

  /**
   * Null print stream to redirect std streams
   */
  public static final PrintStream NULL_PRINT_STREAM = new PrintStream(new NullOutputStream(), true);

  private static final Pattern PATTERN_HOST_PORT = Pattern.compile("^(\\w|\\.|\\-)+\\:\\d+$");

  /**
   * @param url String expression of MBean server URL or abbreviation like localhost:9991
   * @param jpm Java process manager to get process URL
   * @return Parsed JMXServerURL
   * @throws IOException IO error
   */
  public static JMXServiceURL getUrl(String url, JavaProcessManager jpm) throws IOException {
    if (StringUtils.isEmpty(url)) {
      throw new IllegalArgumentException("Empty URL is not allowed");
    } else if (NumberUtils.isDigits(url) && jpm != null) {
      Integer pid = Integer.parseInt(url);
      JavaProcess p;

      p = jpm.get(pid);
      if (p == null) {
        throw new NullPointerException("No such PID " + pid);
      }
      if (!p.isManageable()) {
        p.startManagementAgent();
        if (!p.isManageable()) {
          throw new IllegalStateException("Managed agent for PID " + pid + " couldn't start. PID "
              + pid + " is not manageable");
        }
      }
      return new JMXServiceURL(p.toUrl());

    } else if (PATTERN_HOST_PORT.matcher(url).find()) {
      return new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + url + "/jmxrmi");
    } else {
      return new JMXServiceURL(url);
    }
  }

  /**
   * Check if string value is <code>null</code>
   *
   * @param s String value
   * @return True if value is <code>null</code>
   */
  public static boolean isNull(String s) {
    return StringUtils.equalsIgnoreCase(NULL, s) || StringUtils.equals("*", s);
  }

  /**
   * Parse given string expression to expected type of value
   *
   * @param expression String expression
   * @param type Target type
   * @return Object of value
   */
  public static Object parse(String expression, String type) {
    if (expression == null || StringUtils.equalsIgnoreCase(NULL, expression)) {
      return null;
    }
    Class<?> c;
    try {
      c = ClassUtils.getClass(type);
      if (c.isArray()) {
        ArrayConverter converter = new ArrayConverter(c, new StringConverter());
        converter.setAllowedChars(new char[]{'.', '-', '='});
        ConvertUtils.register(converter, c);
      }
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException("Type " + type + " isn't valid", e);
    }
    if (c == String.class) {
      return expression;
    }
    if (StringUtils.isEmpty(expression)) {
      return null;
    }
    return ConvertUtils.convert(expression, c);
  }

  private SyntaxUtils() {}
}
