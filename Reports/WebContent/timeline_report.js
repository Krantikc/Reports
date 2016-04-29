cApplication = { 
		DATE_FORMAT: 'MMM dd, yy',
		mInit: function() {

			$('#date').datepicker({
				dateFormat: 'M d, y',
				showOtherMonths: true,
			    selectOtherMonths: true,
			    changeMonth: true,
			    changeYear: true
			});
			$('#date').datepicker('setDate', new Date());
			
			$('#userId').val('91101');
			$('#username').val('SENTHIL SHANMUGASUNDARAM');
			$('#reports-data').hide();
			
			$('#back-btn').hide();

			var lUserId = $('#userId').val();
			var lDate = new Date(Date.parse($('#date').val()));
			var lFrequency = 'daily';

			cApplication.mSubmitForm(lUserId, lDate, lFrequency);
		},
		mFilterAttendance: function(pAttendanceDetails, pRequestDetails) {
			var lOptions = {
				chart: {
				    height: cReports.users.USERS.length * 30 + 80,
				    width: 1000
				},
				xAxis: {
				    title: {
				    	text: 'Time'
				    }
				}
			};

			$('#monthly-attendance').html('');
			$('.no-data').hide();
			$('#reports-data').show();
			var lIsPacked = false || $('#packed').prop('checked');
			if (cReports.users.USERS.length > 0) {

				var lInitOptions = {
					officeTimings: pAttendanceDetails.officeTimings, 
					details: pRequestDetails
				};
				
				cReports.init(lInitOptions);  // To initialize office timings and store the request into cache
					
				cReports
					.charts
					.mDrawChart('charts-div', 
								lOptions, 
								pAttendanceDetails, 
								'xrange', 
								lIsPacked);	
				
				if (cReports.util.REQUEST_DETAILS.length > 1) {
					$('#back-btn').show();
				} else {
					$('#back-btn').hide();
				}
				
			} else {
				$('#reports-data').hide();
				$('.no-data').show();
			}
			$('#loading-mask').hide();
		},		
		mGetAttendance: function(pUserId, pDate, pFrequency) {
			var lUserIDs = [];
			$.each(cReports.users.USERS, function(i, lUser) {
				lUserIDs.push(lUser.userId);
			});
			$.ajax({
				//url: config.endPoint + 'api/v1/attendance/subordinates/main_swipes/' + pUserId + '/' + pDate ,
				//url: config.endPoint + 'api/v1/users/subordinates/' + pUserId ,
				url: config.endPoint + 'api/v1/attendance/subordinates/main_swipes/' + pDate,
				type: 'POST',
				data: JSON.stringify({ usersList: lUserIDs }),
				contentType: 'application/x-www-form-urlencoded;charset=UTF-8',
				dataType: 'json',
				success: function(lResult) {
					//cApplication.mGetUsersList(lResult, pFrequency);
					var lRequestDetails = {
						userId: pUserId,
						beginDate: pDate,
						endDate: pDate,
						frequency: pFrequency,
						subordinates: true
					};
					cApplication.mFilterAttendance(lResult, lRequestDetails);
				},
				error: cApplication.mErrorHandler
			});
		},
		mGetUsersList: function(pUserId, pDate, pFrequency) {
			$.ajax({
				url: config.endPoint + 'api/v1/users/subordinates/' + pUserId + '/',
				type: 'GET',
				dataType: 'json',
				success: function(lResult) {
					//cReports.users.mStoreUsers(lResult.usersList);
					//cApplication.mFilterAttendance(lAttendanceDetails, pFrequency);
					
					
					$('#monthly-attendance').html('');
					$('.no-data').hide();
					$('#reports-data').show();
					
					//cApplication.mGetAttendance(pUserId, pDate, pFrequency);
					
					// Div Id for Chart (Mandatory)
					var lDivId = 'charts-div'; 
					
					// Chart options (Not Mandatory. Sets to default values if not defined)
					var lOptions = {
						chart: {
						    width: 'auto',
						    height: lResult.usersList.length * 30 + 80
						},
						title: ''
						/*,
						series: {
							pointWidth: 20
						}*/
					}; 
					
					// Details to be sent for initializing report
					var lRequestDetails = {
							'frequency': pFrequency,
							'beginDate': pDate,
							'endDate': pDate,
							'userId': pUserId
					};
					var lInitOptions = {
						officeDetails: cHRDMS.OFFICE_DETAILS,
						details: lRequestDetails
					};
						
					//cReports.init(lInitOptions);
				
					var lIsPacked = false || $('#packed').prop('checked');
					
					// Components needs to be rendered on the page
					var lComponents = [{
						type: 'chart',
						id: lDivId,
						options: {
							//chartType: 'xrange',
							isPacked: lIsPacked,
							chartOptions: lOptions
						}
					}];
					
					// Generates the timeline report
					cReports.mGetTimelineReport(pDate, lComponents, lInitOptions, lResult.usersList, pUserId);

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
				} else if (lField.name !== 'date' && /^[a-zA-Z0-9- ]*$/.test(lField.value) == false) {
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
			var lDate = new Date(Date.parse(pForm.date.value));
			
			/*if (lDate.getTime() > new Date().getTime()) {
				alert('Date cannot be greater than today');
				return false;
			}*/
			
			return true;
		},
		mSubmitForm: function(pUserId, pDateVal, pFrequency) {
			var lUserId = pUserId || $('#userId').val();
			var lUsername = $('#username').val();
			var lDate = pDateVal || pDateVal != undefined ? pDateVal.toString(cApplication.DATE_FORMAT) : undefined;
			
			var lUserIdJSON = {name: 'userId', value: lUserId, fieldLabel: 'Employee Id'};
			var lUsernameJSON = {name: 'username', value: lUsername, fieldLabel: 'Employee Name'};
			var lDateJSON = {name: 'date', value: lDate, fieldLabel: 'Date'};
			var lReportForm = {	
								  required: [ lUserIdJSON, lUsernameJSON, lDateJSON ], 
								  date: lDateJSON
						     };
			var isFormValid = cApplication.mValidateForm(lReportForm);
			if (isFormValid) {
				$('#loading-mask').show();
				//cApplication.mGetAttendance(lUserId, lDate, pFrequency);
				cApplication.mGetUsersList(lUserId, lDate, pFrequency);
				return true;
			}	
		}
};

$(document).ready(function() {
	cApplication.mInit();
	$('#submit-btn').click(function() {
		var lUserId = $('#userId').val();
		var lDate = new Date(Date.parse($('#date').val()));
		//if(userId!=='') {
		cApplication.mSubmitForm(lUserId, lDate, 'daily');
		//}
	});
	
	$('#date').change( function(date) {

		var lSelectedDate = new Date(Date.parse($(this).val()));
	    var lUserId = $('#userId').val();
		if(lUserId!=='') {
			cApplication.mSubmitForm(lUserId, lSelectedDate, 'daily');
		}
	});
	
	$('#packed').click(function() {
		var lUserId = $('#userId').val();
		var lDate = new Date(Date.parse($('#date').val()));
		if(lUserId!=='') {
			cApplication.mSubmitForm(lUserId, lDate, 'daily');
		}
	});
	
	document.addEventListener('requestreload', function(e, opts) {
		//$('#date').datepicker( 'setDate', e.detail.beginDate);
		
		//var lUser = cReports.users.mGetUserById(e.detail.userId);
		//$('#userId').val(lUser.userId);
		//$('#username').val(lUser.name);
		cApplication.mSubmitForm(e.detail.userId, 
								 e.detail.beginDate.toString(cApplication.DATE_FORMAT), 
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
		
		$('#date').datepicker( 'setDate', lRequestDetails.beginDate);
		
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
            var lSelectedDate = new Date(Date.parse($('#date').val()));
		    var lUserId = $('#userId').val();
  		    cApplication.mSubmitForm(lUserId, lSelectedDate, 'daily');
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
					var lSelectedDate = new Date(Date.parse($('#date').val()));
				    var lUserId = $('#userId').val();
		  		    cApplication.mSubmitForm(lUserId, lSelectedDate, 'daily');
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