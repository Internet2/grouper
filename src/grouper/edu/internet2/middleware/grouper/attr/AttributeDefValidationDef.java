package edu.internet2.middleware.grouper.attr;


/**
 * definitions of validations
 * @author mchyzer
 */
public enum AttributeDefValidationDef {
  
  /** length in chars must be a certain value, not less not more */
  exactLength,

  /** length in chars must be at least this amount */
  minLength,

  /** length in chars cannot be more than this amount */
  maxLength,

  /** validate based on regex */
  regex,
  
  /** if the value is required when the attribute is assigned */
  required,
  
  /** if this is a date (day/month/year) */
  date,
  
  /** if thids is a date (day/month/year) and time (to seconds) */
  dateTimeSeconds,
  
  /** if this is a date (day/month/year) and time (to minutes) */
  dateTimeMinutes,
  
  /** formatting of dateTime, the mask.  Like a java SimpleDateFormat, e.g. mm/dd/ccyy */
  dateTimeMask,
  
  /** dont trim the value before saving */
  dontTrim,
  
  /** number of decimal places on a number */
  numberDecimalPlaces,
  
  /** number must be greater than arg0 */
  numberGreaterThan,
  
  /** number must be greater than or equal to arg0 */
  numberGreaterThanOrEqual,
  
  /** input must be less than arg0 */
  numberLessThan,
  
  /** input must be less than or equal to arg0 */
  numberLessThanOrEqual,
  
  /** on the screen display the number with commas.  Store it without commas */
  numberFormatWithCommas,
  
  /** onorafter yyyymmdd arg0 value */
  dateOnOrAfter,
  
  /** onorbefore yyyymmdd arg0 value */
  dateRangeOnOrBefore,
  
  /** truncate the number instead of rounding if too many decimals */
  numberTruncate,
  
  /** make the string caps */
  stringToUpper,
  
  /** make the string to lower */
  stringToLower,
  
  /** capitalize words so each is lower and starts with upper */
  stringCapitalizeWords,
  
  /** strip out chars which arent in the regex in arg0 */
  stringStripViaIncludeRegex,
  
  /** strip out chars which are in the regex in arg0 */
  stringStripViaExcludeRegex,
  
  /** if this is a timestamp with time and millis */
  dateTimeMillis;
  
}
