package asst.dim;

import asst.dbcommon.AColumn;
import asst.dbcommon.ATable;
import asst.dim.DIM;

/**
 * Class to test database mapping annotations and validations
 * @author Material Gain
 * @since 2014 02
 */
@ATable(tableName="theTable", primaryKeyColumn="UUID")
public class DBPojo {
  @AColumn(columnName="integer", writeZeroAsNull=true)
  public int integer;
  @AColumn(columnName="doub", writeZeroAsNull=true)
  public double doubleV;
  @AColumn(columnName="long", writeZeroAsNull=true)
  public long longV;
  @AColumn(columnName="float", writeZeroAsNull=true)
  public float floatV;
  @AColumn(columnName="strang", notWriteEmpty=true)
  @DIM("URL")
  public String strang;
  @AColumn(columnName="strang1")
  @DIM("email")
  public String strang1;
  @AColumn(columnName="strang2")
  public String strang2;
  @AColumn(columnName="UUID", primaryKey=true)
  public String UUID;
}
