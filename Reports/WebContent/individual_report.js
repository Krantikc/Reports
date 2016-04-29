cApplication = {
		DATE_FORMAT: 'MMM dd, yy',
		COMBO_FIELDS: {
			fields: [
			  {
				  id: 'frequency-name', 
				  url: '', 
				  rootElement: 'frequencyList', 
				  frequencyList: [{key: 'monthly', value: 'Monthly'},{ key: 'weekly', value: 'Weekly'}, {key:'daily', value: 'Daily'}], 
				  defaultValue: {key: 'monthly', value: 'Monthly'}
			  }, 
			],
			details: {
				  'org-name': {defaultValue: '', optionAll: true},
				  'brc-name': {defaultValue: '', optionAll: true},
				  'dept-name': {defaultValue: '', optionAll: true},
				  'rept-mgr-name': {defaultValue: '', optionAll: true},
				  'frequency-name': { defaultValue: {key: 'monthly', value: 'Monthly'}, optionAll: false}
    		 }
		},
		mInit: function() {

			cApplication.mInitializeCombos();
			cApplication.mGetDataForCombos();
			var lDatePickrOptions = {
				dateFormat: 'M d, y',
				showOtherMonths: true,
			    selectOtherMonths: true,
			    changeMonth: true,
			    changeYear: true	
			};
			
			$('#fromDate').datepicker(lDatePickrOptions);
			$('#toDate').datepicker(lDatePickrOptions);
			
			var lDefaultFromBound = new Date((new Date().getFullYear() - 1), 0, 1);
			var lDefaultFromDate = new Date(new Date().getFullYear(), 0, 1);
			
			$('#fromDate').datepicker('setDate', lDefaultFromDate);
			$('#toDate').datepicker('setDate', new Date());
			
			var lDateRangeOptions = {
					formatter: function(val) {
				        return val.toString(cApplication.DATE_FORMAT);
				    },
				    bounds: {
				        min: lDefaultFromBound,
				        max: new Date()
				    },
				    defaultValues: {
				        min: lDefaultFromDate,
				        max: new Date()
				    }
			};

			$('#date-range-slider').dateRangeSlider(lDateRangeOptions);
			
			$('#back-btn').hide();
		},
		mInitializeCombos: function() {
			$.each(cApplication.COMBO_FIELDS.fields, function(index, lComboField) {
				var lId = lComboField.id;
				$( '#' + lId ).combobox();
			    $( '#toggle' ).click(function() {
			      $( '#' + lId ).toggle();
			    });
			});	
		},
		mPopulateCombo: function(pId, pComboData) {
			var lSelectElement = $('#'+pId);
			lSelectElement.html('');
			if (cApplication.COMBO_FIELDS.details[pId].optionAll) {
				lSelectElement.append('<option value="all">All</option>');
			}
			$.each(pComboData, function(index, option) {
				lSelectElement.append('<option value="' + option.key + '">' + option.value + '</option>');
			});
			var lDefaultValue = cApplication.COMBO_FIELDS.details[pId] !== undefined ? cApplication.COMBO_FIELDS.details[pId].defaultValue : '';
			if (lDefaultValue !== '') {
				$( '#'+pId ).val(cApplication.COMBO_FIELDS.details[pId].defaultValue.key).combobox('refresh');
			} else {
				$( '#'+pId ).val($( '#'+pId )[0][0].value).combobox('refresh');
			}
		},
		mGetDataForCombos: function() {
			$.each(cApplication.COMBO_FIELDS.fields, function(index, lComboField) {
				if(lComboField.url !== '') {
					$.ajax({
						url: lComboField.url,
						type: 'GET',
						dataType: 'json',
						success: function(lResult) {
							var lComboData = lResult[lComboField.rootElement];
							cApplication.mPopulateCombo(lComboField.id, lComboData);
						},
						error: function() {
							console.log('Error');
						}
					});
				} else {
					var lComboData = lComboField[lComboField.rootElement];
					cApplication.mPopulateCombo(lComboField.id, lComboData);
				    $( '#'+lComboField.id ).val(lComboField.defaultValue.key).combobox('refresh');
				}
			});
		},
		mFilterAttendance: function(pResult, pRequestDetails) {
			$('#monthly-attendance').html('');
			$('.no-data').hide();
			$('#reports-data').show();
			var lAttendanceDetailsList = pResult;
			if (lAttendanceDetailsList.userAttendance.length > 0) {
				

				var lInitOptions = {
					officeTimings: pResult.officeTimings, 
					details: pRequestDetails
				};
				
				cReports.init(lInitOptions);  // To initialize office timings and store the request into cache
				
				var lChartObj = cReports    // mDrawChart() returns the generated chart obj.
									.charts
									.mDrawChart('charts-div', 
											    {chart: {height: 400, width: 900}}, 
												lAttendanceDetailsList, 
												'column', 
												false, 
												pRequestDetails.frequency);	
				
				var lTableOptions = {
					height: 245,
					sortable: false,
					pagination: false,
					search: false,
					fixedColumn: true,
					columnReorder: false,
					columnResize: true
				};
				var lAttendanceDetails = cReports
										 	.grid
										 	.mRenderAttendance('grid-div',  
										 					   lAttendanceDetailsList, 
										 					   pRequestDetails.frequency, 
										 					   lTableOptions);
				
				var lSummaryOptions = {
					//useStyleSheet: true,
					position: 'absolute',
					//align: 'right',
					//width: '300px',
					//backgroundColor: 'yellow',
					//headerBackgroundColor: 'pink',
					margin: '-270px 0 0 0',
					right: window.outerWidth * 0.075 + 'px',
					//labels: {
						//summary: 'Summary',
						//name: 'Name: ',
						//period: 'Period: ',
						//stdWorkingDays: 'Std. Working Days',
						//addDays: 'Additional days of work',
						//vacation: 'Vacation',
						//effHrsReqd: 'Effective Hrs',
						//effHrsActual: 'Effective Hrs',
						//avgEffHrsReqd: 'Avg Eff Hrs',
						//avgEffHrsActual: 'Avg Eff Hrs'
								
					//}
				};
				cReports.grid.mRenderSummary('summary-div',  lAttendanceDetails.summaryTableData, lSummaryOptions);
				//cReports.dailyAttendanceData = pResult.subordinatesAttendance;
				//cReports.mRenderAttendance( pResult, pFrequency);
				//$('#show-table-btn').show();
				

			} else {
				$('#reports-data').hide();
				$('.no-data').show();
			}
			$('#loading-mask').hide();
		},		
		mGetAttendance: function(pUserId, pFromDate, pToDate, pFrequency) {
			$.ajax({
				url: config.endPoint + 'api/v1/attendance/' + pUserId + '/' + pFromDate + '/' + pToDate + '/' + pFrequency,
				type: 'GET',
				dataType: 'json',
				success: function(lResult) {
					var lRequestDetails = {
						userId: pUserId,
						beginDate: pFromDate,
						endDate: pToDate,
						frequency: pFrequency,
						subordinates: false
					};
					cApplication.mFilterAttendance(lResult, lRequestDetails);
				},
				error: cApplication.mErrorHandler
			});
		},
		mGetUser: function(pUserId, pFromDate, pToDate, pFrequency) {
			$.ajax({
				url: config.endPoint + 'api/v1/users/' + pUserId,
				type: 'GET',
				dataType: 'json',
				success: function(lResult) {
					//cReports.users.mStoreUsers([lResult.user]);
					//cApplication.mFilterAttendance(lAttendanceDetails, pFrequency);

					var lRequestDetails = {
							'frequency': pFrequency,
							'beginDate': pFromDate,
							'endDate': pToDate,
							'userId': pUserId,
							'subordinates': false
					};
					
					var lInitOptions = {
							holidaysDetails: cHRDMS.HOLIDAYS_DETAILS,
							officeDetails: cHRDMS.OFFICE_DETAILS,
							details: lRequestDetails
						};
					
					// Div id for Chart (Mandatory)
					var lChartDivId = 'charts-div'; 
					
					// Chart Options
					var lChartOptions = { // Not mandatory. It takes default values
						chart: {
							width: 900 
						}
					}; 
					
					// Div Id for Table Grid (Mandatory)
					var lGridDivId = 'grid-div';
					
					var lTableOptions = { // Not mandatory. It takes default values
							
							pagination: false,
							search: false
						};
					
					// Summary Table options 
					var lSummaryOptions = { // Not mandatory. It takes default values
							//useStyleSheet: true,
							position: 'absolute',
							margin: '-270px 0 0 0',
							right: window.outerWidth * 0.075 + 'px',
							
						};
				
					// Div Id for Summary Table  (Mandatory)
					var lSummaryDivId = 'summary-div';
					
					
					// Components to render on the page
					var lComponents = [{
						type: 'chart',
						id: lChartDivId,
						options: {
							//chartType: 'column',
							chartOptions: lChartOptions
						}
					},{
						type: 'grid',
						id: lGridDivId,
						options: {
							//gridOptions: lTableOptions
						}
					},{
						type: 'summary',
						id: lSummaryDivId,
						options: {
							summaryOptions: lSummaryOptions
						}
					}];

					cReports.mGetUserReport(lResult.user, pFromDate, pToDate, lComponents, lInitOptions);
					//cApplication.mGetAttendance(pUserId, pFromDate, pToDate, pFrequency);
					
				},
				error: cApplication.mErrorHandler
			});
		},
		mShowCalendar: function(pInputType) {
			$('#' + pInputType).datepicker( 'show' );
		},
		mNextDate: function() {
			var lDate = new Date(Date.parse($('#date').val()));
			lDate = lDate.add(1).days();
			var lUserID = $('#userId').val();
			
			var lIsSubmitted = cApplication.mSubmitForm(lUserID, lDate, 'daily');
			if (lIsSubmitted) {
				$('#date').val(lDate.toString(cApplication.DATE_FORMAT));
			}
		},
		mPrevDate: function() {
			var lDate = new Date(Date.parse($('#date').val()));
			lDate = lDate.add(-1).days();
			var lUserID = $('#userId').val();
			var lIsSubmitted = cApplication.mSubmitForm(lUserID, lDate, 'daily');
			if (lIsSubmitted) {
				$('#date').val(lDate.toString(cApplication.DATE_FORMAT));
			}
		},
		mValidateForm: function(pForm) {
			var lRequiredFields = [];
			var lIllegalEntry = '';
			$('#error-message').html('');
            $.each(pForm.required, function(index, lField) {
				if (!lField.value || lField.value === '' || lField.value.trim() === '') {
					lRequiredFields.push(lField.fieldLabel);
				} else if (lField.name !== 'fromDate' && lField.name !== 'toDate' && /^[a-zA-Z0-9- .]*$/.test(lField.value) == false) {
					lIllegalEntry = 'Invalid input';
        		}
			});
            
            if(lIllegalEntry !== '') {
				alert(lIllegalEntry);
				return false;
			}
			if(lRequiredFields.length > 0) {
				var lRequiredFieldsStr = lRequiredFields.join(', ');
				alert('Please fill the mandatory fields - ' + lRequiredFieldsStr);
				return false;
			}
			var lFromDate = new Date(Date.parse(pForm.fromDate.value));
			var lToDate = new Date(Date.parse(pForm.toDate.value));
	/*		if (lFromDate.getTime() > new Date().getTime()) {
				alert('From Date cannot be greater than today');
				return false;
			}
			if (lToDate.getTime() > new Date().getTime()) {
				alert('To Date cannot be greater than today');
				return false;
			}*/
			if (lFromDate.getTime() > lToDate.getTime()) {
				alert('From Date cannot be greater than To Date');
				return false;
			}
			return true;
		},
		mSubmitForm: function(pUserId, pFromDateVal, pToDateVal, pFrequency, pIsAutosubmit) {
	
			var lUserId = pUserId || $('#userId').val();
			var lUsername = $('#username').val();
			var lDateRange = $('#date-range-slider').dateRangeSlider('values');
			var lFromDate = pFromDateVal || pFromDateVal != undefined ? pFromDateVal.toString(cApplication.DATE_FORMAT) : undefined;
			var lToDate = pToDateVal || pToDateVal != undefined ? pToDateVal.toString(cApplication.DATE_FORMAT) : undefined;
			if (!lFromDate && !lToDate) {
				lFromDate = lDateRange.min.toString(cApplication.DATE_FORMAT);
				lToDate = lDateRange.max.toString(cApplication.DATE_FORMAT);
			}
			var lUserIdJSON = {name: 'userId', value: lUserId, fieldLabel: 'Employee Id'};
			var lUsernameJSON = {name: 'username', value: lUsername, fieldLabel: 'Employee Name'};
			var lFromDateJSON = {name: 'fromDate', value: lFromDate, fieldLabel: 'From Date'};
			var lToDateJSON = {name: 'toDate', value: lToDate, fieldLabel: 'To Date'};
			var lReportForm = {	
								  required: [ lUserIdJSON, lUsernameJSON, lFromDateJSON, lToDateJSON ], 
								  fromDate: lFromDateJSON, toDate: lToDateJSON
						     };
			var lFrequency = pFrequency || $('#frequency-name').val();
			var isFormValid = cApplication.mValidateForm(lReportForm);
			if (isFormValid) {
				$('#loading-mask').show();
				var lFreq = lFrequency.toLowerCase();
				cApplication.mGetUser(lUserId, lFromDate, lToDate, lFreq);
			}
		}
};

$(document).ready(function() {
	cApplication.mInit();
	$('#submit-btn').click(function() {
	    var lUserId = $('#userId').val();
		var lDateRange = $('#date-range-slider').dateRangeSlider('values');
		//if(lUserId!=='') {
			cApplication.mSubmitForm(lUserId, lDateRange.min, lDateRange.max);
		//}
	});
	
	document.addEventListener('requestreload', function(e, opts) {

		cApplication.mSubmitForm(e.detail.userId, 
								 e.detail.beginDate.toString(cApplication.DATE_FORMAT), 
								 e.detail.endDate.toString(cApplication.DATE_FORMAT), 
								 e.detail.frequency.toLowerCase());
	});
	
	document.addEventListener('requestcomplete', function(e, opts) {

		var lSuccess = e.detail.success;
		$('#loading-mask').hide();
		if (!lSuccess) {
			alert('ERROR: error while fetching data');
			console.error('ERROR: error while fetching data');
		}
		
		var lRequestDetails = e.detail.requestDetails;
		$('#fromDate').datepicker( 'setDate', lRequestDetails.beginDate);
		$('#toDate').datepicker( 'setDate', lRequestDetails.endDate);
		$('#date-range-slider').dateRangeSlider('values', 
												new Date(Date.parse(lRequestDetails.beginDate)), 
												new Date(Date.parse(lRequestDetails.endDate)));
		$('#frequency-name').val(lRequestDetails.frequency).combobox('refresh');
		
		var lUserId = lRequestDetails.userId;
		var lUser = cReports.users.mGetUserById(lUserId);
		$('#userId').val(lUser.userId);
		$('#username').val(lUser.name);

		
		if (cReports.util.REQUEST_DETAILS.length > 1) {
			$('#back-btn').show();
		} else {
			$('#back-btn').hide();
		}
	});
	
	$('#date-range-slider').bind('userValuesChanged', function(e, data) {
		  var lUserId = $('#userId').val();
		  $('#fromDate').datepicker( 'setDate', data.values.min );
		  $('#toDate').datepicker( 'setDate', data.values.max );
		  if(lUserId!=='') {
			  cApplication.mSubmitForm(lUserId, data.values.min, data.values.max);
		  }
    });
	


	$('#fromDate').change( function(date) {
			var lDateBounds = $('#date-range-slider').dateRangeSlider("bounds");
			var lMinDateBound = lDateBounds.min;
			var lSelectedDate = new Date(Date.parse($(this).val()));
			if (lSelectedDate.getTime() < lMinDateBound.getTime()) {
				lSelectedDate.setDate(1);
				$('#date-range-slider').dateRangeSlider('bounds', lSelectedDate, lDateBounds.max);	
			}
		    $('#date-range-slider').dateRangeSlider('min', new Date(Date.parse($(this).val())));
		    var lUserId = $('#userId').val();
			var lDateRange = $('#date-range-slider').dateRangeSlider('values');
			if(lUserId!=='') {
				cApplication.mSubmitForm(lUserId, lDateRange.min, lDateRange.max);
			}
	});
	$('#toDate').change( function(date) {
		var lDateBounds = $('#date-range-slider').dateRangeSlider("bounds");
		var lMaxDateBound = lDateBounds.max;
		var lSelectedDate = new Date(Date.parse($(this).val()));
		if (lSelectedDate.getTime() > lMaxDateBound.getTime()) {
			lSelectedDate.setMonth(lSelectedDate.getMonth() + 1);
			$('#date-range-slider').dateRangeSlider('bounds', lDateBounds.min, lSelectedDate);	
		}
		$('#date-range-slider').dateRangeSlider('max', new Date(Date.parse($(this).val())));
	    var lUserId = $('#userId').val();
		var lDateRange = $('#date-range-slider').dateRangeSlider('values');
		if(lUserId!=='') {
			cApplication.mSubmitForm(lUserId, lDateRange.min, lDateRange.max);
		}
	});
	
	$('#username').autocomplete({
        source: function( req, resp ) {
            $.get( config.endPoint + 'api/v1/users/JSON', {
                searchPattern: req.term
            }, function(lResult) {
                resp( lResult.user );
            }, 'JSON' );
        },
        minLength: 1,
        select: function(event, ui) {
            $('#userId').val(ui.item.id);
            var lUserId = $('#userId').val();
			var lDateRange = $('#date-range-slider').dateRangeSlider('values');
			if(lUserId!=='') {
				cApplication.mSubmitForm(lUserId, lDateRange.min, lDateRange.max);
			}
        },
        change: function (event, ui) {
            if (ui.item === null) {
            	if (/^[a-zA-Z0-9- ]*$/.test($(this).val()) == false) {
        			alert('Invalid input');
        		}
            }
        }
    });
	
	$('#userId').autocomplete({
        source: function( req, resp ) {
            $.get( config.endPoint + 'api/v1/users/JSONID', {
                searchPattern: req.term
            }, function(lResult) {
                resp( lResult.user );
            }, 'JSON' );
        },
        minLength: 1,
        select: function(event, ui) {
        	var lUserId = ui.item.id;
			
        	$.ajax({
				url: config.endPoint + 'api/v1/users/'+ lUserId,
				type: 'GET',
				dataType: 'json',
				success: function(lResult){
					$('#username').val(lResult.user.name);
					var lUserId = $('#userId').val();
					var lDateRange = $('#date-range-slider').dateRangeSlider('values');
					if(lUserId!=='') {
						cApplication.mSubmitForm(lUserId, lDateRange.min, lDateRange.max);
					}
				},
				error: cApplication.mErrorHandler
			});
        },
        change: function (event, ui) {
            if (ui.item === null) {
            	if (/^[a-zA-Z0-9- ]*$/.test($(this).val()) == false) {
        			alert('Invalid input');
        		}
            }
        }
    }).autocomplete( 'instance' )._renderItem = function( ul, item ) {
	      return $( '<li>' )
	        .append( '<a>' + item.id + '</a>' )
	        .appendTo( ul );
	    };

	$('#userId').on('input', function() {
		$('#username').val('');
		var lUserId = $(this).val();
		if(lUserId.length > 4) {
			$.ajax({
				url: config.endPoint + 'api/v1/users/'+ lUserId,
				type: 'GET',
				dataType: 'json',
				success: function(lResult){
					$('#username').val(lResult.user.name);
				},
				error: cApplication.mErrorHandler
			});
		}
	});
});
