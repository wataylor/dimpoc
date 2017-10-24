/**
 * 
 */
package asst.dim;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import asst.dim.ValidateDIMs;

/**
 * @author Material Gain
 * @since 2014 03
 */
public class DIMjUnit {

  DBPojo dbpojo;

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testValidateURL() throws Exception {
    DBPojo poj = new DBPojo();
    boolean result;
    /* The string builder accumulates error messages.  It is passed to
     * each DIM in succession as the various fields are validated.  */
    StringBuilder sb = new StringBuilder();
    poj.strang = "google.com";
    Field fld = DBPojo.class.getField("strang");
    result = ValidateDIMs.validateURL(fld, poj, sb);
    /* This combines both validation and showing how a field can be
     * converted to canonical form during validation.  Spaces would be
     * stripped out of CC numbers and SSN, for example.*/
    assertEquals(poj.strang, "http://google.com");
    assertTrue(result);
    /* This test seems a bit counterintuitive, but the specification
     * says that a query string by itself is a valid URL.  */
    poj.strang = "?" + "!@#$%^&*";
    result = ValidateDIMs.validateURL(fld, poj, sb);
    assertTrue(result);
    poj.strang = "runble\"'.com";
    result = ValidateDIMs.validateURL(fld, poj, sb);
    assertEquals(poj.strang, "http://runble'.com");
    assertTrue(result);
    // System.out.println(poj.strang + " " + sb.toString());
  }

  /**
   * @throws Exception
   */
  @Test
  public void testValidateEmail() throws Exception {
    DBPojo poj = new DBPojo();
    Field fld = DBPojo.class.getField("strang1");
    StringBuilder sb = new StringBuilder();
    poj.strang1 = "me@paymentech.com";
    assertTrue(ValidateDIMs.validateEmail(fld, poj, sb));
    poj.strang1 = "webmaster@m�ller.de";
    assertTrue(ValidateDIMs.validateEmail(fld, poj, sb));
    poj.strang1 = "Chuck Norris <gmail@chucknorris.com>";
    assertTrue(ValidateDIMs.validateEmail(fld, poj, sb));
    poj.strang1 = "matteo@78.47.122.114";
    assertTrue(ValidateDIMs.validateEmail(fld, poj, sb));
    poj.strang1 = "user@.invalid.com";
    assertFalse(ValidateDIMs.validateEmail(fld, poj, sb));
    //System.out.println(sb);
  }

  @Test
  public void testValidateDIMs() throws Exception {
    DBPojo poj = new DBPojo();
    String result;
    poj.strang1 = "me@paymentech.com";
    poj.strang = "google.com";
    result = ValidateDIMs.validateObject(poj);
    assertTrue(result == null);
    poj.strang = "thhppss://Bad Oil";
    poj.strang1 = "user@.invalid.com";
    result = ValidateDIMs.validateObject(poj);
    assertFalse(result == null);
    // System.out.println(result);

  }
}
