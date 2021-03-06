/**
 * This file depends on form.js.
 * 
 * Format for the span/input identifiers for selectors:
 * 
 * {dataelementid}-{optioncomboid}-val // data value 
 * {dataelementid}-dataelement name of data element 
 * {optioncomboid}-optioncombo // name of category option combo 
 * {dataelementid}-cell // table cell for data element name
 * {dataelementid}-{optioncomboid}-min // min value for data value
 * {dataelementid}-{optioncomboid}-max // max value for data value
 */

// -----------------------------------------------------------------------------
// Save
// -----------------------------------------------------------------------------

var FORMULA_PATTERN = /\[.+?\]/g;
var SEPARATOR = '.';

function updateDataElementTotals()
{
	var currentTotals = [];
	
	$( 'input[name="total"]' ).each( function( index )
	{
		var targetId = $( this ).attr( 'dataelementid' );
		
		var totalValue = new Number();
		
		$( 'input[name="entryfield"]' ).each( function( index )
		{	
			var key = $( this ).attr( 'id' );
			var entryFieldId = key.substring( 0, key.indexOf( '-' ) );
			
			if ( targetId && $( this ).attr( 'value' ) && targetId == entryFieldId )
			{
				totalValue += new Number( $( this ).attr( 'value' ) );
			}
		} );
		
		$( this ).attr( 'value', totalValue );
	} );
}

/**
 * Updates all indicator input fields with the calculated value based on the
 * values in the input entry fields in the form.
 */
function updateIndicators()
{
    $( 'input[name="indicator"]' ).each( function( index )
    {
        var indicatorId = $( this ).attr( 'indicatorid' );

        var formula = indicatorFormulas[indicatorId];

        var expression = generateExpression( formula );

        if ( expression )
        {
	        var value = eval( expression );
	        
	        value = isNaN( value ) ? '-' : roundTo( value, 1 );
	
	        $( this ).attr( 'value', value );
        }
    } );
}

/**
 * Parses the expression and substitues the operand identifiers with the value
 * of the corresponding input entry field.
 */
function generateExpression( expression )
{
    var matcher = expression.match( FORMULA_PATTERN );

    for ( k in matcher )
    {
        var match = matcher[k];

        // Remove brackets from expression to simplify extraction of identifiers

        var operand = match.replace( /[\[\]]/g, '' );

        var dataElementId = operand.substring( 0, operand.indexOf( SEPARATOR ) );
        var categoryOptionComboId = operand.substring( operand.indexOf( SEPARATOR ) + 1, operand.length );

        var fieldId = '#' + dataElementId + '-' + categoryOptionComboId + '-val';

        if ( $( fieldId ).length )
        {
            var value = $( fieldId ).val() ? $( fieldId ).val() : '0';

            expression = expression.replace( match, value );
        }
        else // Return null if data element / category option combo not in form
        {	
            return null;
        } 
        
        // TODO signed numbers
    }

    return expression;
}

/**
 * /* Used by default and section forms.
 */
function saveVal( dataElementId, optionComboId )
{
	dataElementId = parseInt( dataElementId );
	optionComboId = parseInt( optionComboId );
	
    var dataElementName = getDataElementName( dataElementId );
    var fieldId = '#' + dataElementId + '-' + optionComboId + '-val';
    var value = $( fieldId ).val();
    var type = getDataElementType( dataElementId );

    $( fieldId ).css( 'background-color', COLOR_YELLOW );

    var periodId = $( '#selectedPeriodId' ).val();

	if ( value == null )
	{
		value = '';
	}

    if ( value != '' )
    {
        if ( type == 'int' || type == 'number' || type == 'positiveNumber' || type == 'negativeNumber' )
        {
            if ( value.length > 255 )
            {
                return alertField( fieldId, i18n_value_too_long + ': ' + dataElementName );
            }
            if ( type == 'int' && !isInt( value ) )
            {
                return alertField( fieldId, i18n_value_must_integer + ': ' + dataElementName );
            }
            if ( type == 'number' && !isRealNumber( value ) )
            {
                return alertField( fieldId, i18n_value_must_number + ': ' + dataElementName );
            }
            if ( type == 'positiveNumber' && !isPositiveInt( value ) )
            {
                return alertField( fieldId, i18n_value_must_positive_integer + ': ' + dataElementName );
            }
            if ( type == 'negativeNumber' && !isNegativeInt( value ) )
            {
                return alertField( fieldId, i18n_value_must_negative_integer + ': ' + dataElementName );
            }
            if ( isValidZeroNumber( value ) )
            {
                // If value = 0 and zero not significant for data element, skip

                if ( significantZeros.indexOf( dataElementId ) == -1 )
                {
                    $( fieldId ).css( 'background-color', COLOR_GREEN );
                    return false;
                }
            }

            var minString = currentMinMaxValueMap[dataElementId + '-' + optionComboId + '-min'];
            var maxString = currentMinMaxValueMap[dataElementId + '-' + optionComboId + '-max'];

            if ( minString && maxString ) // TODO if only one exists?
            {
                var valueNo = new Number( value );
                var min = new Number( minString );
                var max = new Number( maxString );

                if ( valueNo < min )
                {
                    var valueSaver = new ValueSaver( dataElementId, optionComboId, currentOrganisationUnitId, periodId,
                            value, COLOR_ORANGE );
                    valueSaver.save();

                    window.alert( i18n_value_of_data_element_less + ': ' + min + '\n\n' + dataElementName );
                    return;
                }

                if ( valueNo > max )
                {
                    var valueSaver = new ValueSaver( dataElementId, optionComboId, currentOrganisationUnitId, periodId,
                            value, COLOR_ORANGE );
                    valueSaver.save();

                    window.alert( i18n_value_of_data_element_greater + ': ' + max + '\n\n' + dataElementName );
                    return;
                }
            }
        }
    }

    var valueSaver = new ValueSaver( dataElementId, optionComboId, 
    	currentOrganisationUnitId, periodId, value, COLOR_GREEN );
    valueSaver.save();

    updateIndicators(); // Update indicators for custom form
    updateDataElementTotals(); // Update data element totals for custom forms
}

function saveBoolean( dataElementId, optionComboId )
{
    var fieldId = '#' + dataElementId + '-' + optionComboId + '-val';
    var value = $( fieldId + ' option:selected' ).val();

    $( fieldId ).css( 'background-color', COLOR_YELLOW );

    var periodId = $( '#selectedPeriodId' ).val();

    var valueSaver = new ValueSaver( dataElementId, optionComboId, 
    	currentOrganisationUnitId, periodId, value, COLOR_GREEN );
    valueSaver.save();
}

/**
 * Supportive method.
 */
function alertField( fieldId, alertMessage )
{
    $( fieldId ).css( 'background-color', COLOR_YELLOW );
    $( fieldId ).select();
    $( fieldId ).focus();    
    window.alert( alertMessage );

    return false;
}

// -----------------------------------------------------------------------------
// Saver objects
// -----------------------------------------------------------------------------

function ValueSaver( dataElementId_, optionComboId_, organisationUnitId_, periodId_, value_, resultColor_ )
{
    var dataValue = {
        'dataElementId' : dataElementId_,
        'optionComboId' : optionComboId_,
        'organisationUnitId' : organisationUnitId_,
        'periodId' : periodId_,
        'value' : value_,
    };

    var resultColor = resultColor_;

    this.save = function()
    {
        storageManager.saveDataValue( dataValue );

        $.ajax( {
            url: 'saveValue.action',
            data: dataValue,
            dataType: 'json',
            cache: false,
            success: handleSuccess,
            error: handleError
        } );
    };

    function handleSuccess( json )
    {
        var code = json.c;

        if ( code == 0 ) // Value successfully saved on server
        {
        	storageManager.clearDataValueJSON( dataValue );
            markValue( resultColor );
        }
        else if(code == 2)
        {
            markValue( COLOR_RED );
            window.alert( i18n_saving_value_failed_dataset_is_locked );
        }
        else // Server error during save
        {
            markValue( COLOR_RED );
            window.alert( i18n_saving_value_failed_status_code + '\n\n' + code );
        }
    }

    function handleError( jqXHR, textStatus, errorThrown )
    {
        setHeaderMessage( i18n_offline_notification );
        markValue( resultColor );
    }

    function markValue( color )
    {
        $( '#' + dataValue.dataElementId + '-' + dataValue.optionComboId + '-val' ).css( 'background-color', color );
    }
}

//----------------------------------------------------------DistrictStockDataSet--------------------------------------------------

function calculatedDataValueForNextMonthOpeningBallance( selectedInputIdBFPM, selectedInputIdSR, selectedInputIdU, selectedInputIdSD )
	{
		var selectedOrganisationUnit = $( '#selectedOrganisationUnit' ).val();
		var selectedPeriod = $( '#selectedPeriodId' ).val();
	
		var selectedInputId_BFPM_Temp = selectedInputIdBFPM.split( '-' );
		var selectedDataElementID_BFPM = selectedInputId_BFPM_Temp[0];
		var selectedDataElementCOCID_BFPM = selectedInputId_BFPM_Temp[1];
		var selectedDataElementValue_BFPM = document.getElementById(selectedInputIdBFPM).value;

		var selectedInputId_SR_Temp = selectedInputIdSR.split( '-' );
		var selectedDataElementID_SR = selectedInputId_SR_Temp[0];
		var selectedDataElementCOCID_SR = selectedInputId_SR_Temp[1];
		var selectedDataElementValue_SR = document.getElementById(selectedInputIdSR).value;

		var selectedInputId_U_Temp = selectedInputIdU.split( '-' );
		var selectedInputId_U = selectedInputId_U_Temp[0];
		var selectedDataElementCOCID_U = selectedInputId_U_Temp[1];
		var selectedDataElementValue_U = document.getElementById(selectedInputIdU).value;

		var selectedInputId_SD_Temp = selectedInputIdSD.split( '-' );
		var selectedDataElementID_SD = selectedInputId_SR_Temp[0];
		var selectedDataElementCOCID_SD = selectedInputId_SR_Temp[1];
		var selectedDataElementValue_SD = document.getElementById(selectedInputIdSD).value;

		var selectedDataElementTSValue =  ( (selectedDataElementValue_BFPM + selectedDataElementValue_SR)
										- (selectedDataElementValue_U + selectedDataElementValue_SD) );
	  
		var nextMonthExternalPeriodId = getNextMonthExternalPeriodId( selectedPeriod );
		//alert("nextMonthExternalPeriodId:" + nextMonthExternalPeriodId);
	  
		ValueSaver( selectedDataElementID_BFPM, selectedDataElementCOCID_BFPM, organisationUnitId, 
					nextMonthExternalPeriodId, selectedDataElementTSValue, fieldId, resultColor );					
	}

function getNextMonthExternalPeriodId( selectedPeriod )
	{
	   var temp = selectedPeriod.split('_');
	   var temp0 = temp[0];
	   var temp1 = temp[1];
	   var temp2 = temp1.split('-');
	   var temp3 = parseInt(temp2[0]);
	   var temp4 = parseInt(temp2[1]);
	   var temp5 = parseInt(temp2[2]);
	   if(temp4 == 12){
			temp3 = temp3 + 1;
			temp4 = 01;
	   }else{
			temp4 = temp4 + 01;
	   }
	   var temp6 = temp0 + "_" + temp3 + "-" + temp4 + "-" + temp5 ;
	   return temp6;
	}
