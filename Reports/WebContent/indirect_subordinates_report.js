cApplication = {
		mInit: function() {

			$('#date').datepicker({
				dateFormat: 'M d, y',
				showOtherMonths: true,
			    selectOtherMonths: true,
			    changeMonth: true,
			    changeYear: true
			});
			$('#date').datepicker('setDate', new Date());
			
			$('#userId').val('70014');
			$('#username').val('P Ramesh Chandra');
			$('#reports-data').hide();
			$('#show-table-btn').hide();

			var lUserId = $('#userId').val();
			var lDate = new Date(Date.parse($('#date').val()));
			var lFrequency = 'daily';

			cApplication.mSubmitForm(lUserId, lDate, lFrequency);
		},
		mFilterAttendance: function(pResult, pFrequency) {
			$('#monthly-attendance').html('');
			$('.no-data').hide();
			$('#reports-data').show();
			var lAttendanceDetailsList = pResult;
			cApplication.usersList = pResult.usersList;
			if (cApplication.usersList.length > 0) {
				cReports.init({officeTimings: pResult.officeTimings});
				cReports.charts.mDrawChart('charts-div', {xAxis : {title: 'Employees'}}, lAttendanceDetailsList, 'bar');	
				//cReports.dailyAttendanceData = pResult.subordinatesAttendance;
				//cReports.mRenderAttendance( pResult, pFrequency);
				//$('#show-table-btn').show();
			} else {
				$('#reports-data').hide();
				$('.no-data').show();
			}
			$('#loading-mask').hide();
		},		
		mGetAttendance: function(pUserId, pDate, pFrequency) {
			$.ajax({
				url: config.endPoint + 'api/v1/attendance/subordinates/' + pUserId + '/' + pDate ,
				type: 'GET',
				dataType: 'json',
				success: function(lResult) {
					cApplication.mFilterAttendance(lResult, pFrequency);
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
				$('#date').val(lDate.toString('MMM dd, yy'));
			}
		},
		mPrevDate: function() {
			var lDate = new Date(Date.parse($('#date').val()));
			lDate = lDate.add(-1).days();
			var lUserID = $('#userId').val();
			var lIsSubmitted = cApplication.mSubmitForm(lUserID, lDate, 'daily');
			if (lIsSubmitted) {
				$('#date').val(lDate.toString('MMM dd, yy'));
			}
		},
		mValidateForm: function(pForm) {
			var lRequiredFields = [];
			var lIllegalEntry = '';
			$('#error-message').html('');
            $.each(pForm.required, function(index, lField) {
				if (!lField.value || lField.value === '' || lField.value.trim() === '') {
					lRequiredFields.push(lField.fieldLabel);
				} else if (lField.name !== 'date' && /^[a-zA-Z0-9- .]*$/.test(lField.value) == false) {
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
			
			if (lDate.getTime() > new Date().getTime()) {
				alert('Date cannot be greater than today');
				return false;
			}
			
			return true;
		},
		mSubmitForm: function(pUserId, pDateVal, pFrequency) {
			var lUserId = pUserId || $('#userId').val();
			var lUsername = $('#username').val();
			var lDate = pDateVal || pDateVal != undefined ? pDateVal.toString('MMM dd, yy') : undefined;
			
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
				cApplication.mGetAttendance(lUserId, lDate, pFrequency);
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
	
	$('#direct-subordinates').click(function() {	
		var lUserID = $('#userId').val();
		var lDate = new Date(Date.parse($('#date').val()));
		cApplication.mSubmitForm(lUserID, lDate, 'daily');
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