/**
 * @author kcr 2015-12-10
 * This class gives APIs to render attendance reports graph and 
 * few other components like grid table, summary table etc
 */
(function() {
	cReports = {
		/**
		 * Initialises Highcharts to disable UTC standards,
		 * and also initialises the office timings.
		 * Note: This method needs to be called before invoking one of the methods of 'charts' or 'grid' subclass
		 * 		 To synchronize with office timings for every fresh call of 'charts' or 'grid', this method must be called.
		 * @param pOptions	Object	The object holding office timings and params to call methods of 'charts' or 'grid'
		 */	
		init: function(pOptions, pUsersList) {
			Highcharts.setOptions({
			    global: {
			    	useUTC: false
			    }
			});
			var WORKING_HOURS = 9.0;
			var HALF_DAY_REQ_HOURS = 4;
			var FULL_DAY_REQ_HOURS = 8.15;
			var SHIFT_START = '9:00';
			var SHIFT_END = '18:30';
			var BREAK_START = '13:00';
			var BREAK_END = '13:30';
			
			var DATE_FORMAT = 'MMM dd, yy';
			
			
			if (pOptions) {
				//var lOfficeTimings = pOptions.officeDetails[0];
				cReports.util.OFFICE_DETAILS = pOptions.officeDetails;
				/*if (lOfficeTimings) {
					WORKING_HOURS = lOfficeTimings.workingHours || WORKING_HOURS;
					HALF_DAY_REQ_HOURS = lOfficeTimings.requiredHoursForHalfDay || HALF_DAY_REQ_HOURS;
					FULL_DAY_REQ_HOURS = lOfficeTimings.requiredHoursForFullDay || FULL_DAY_REQ_HOURS;
					SHIFT_START = cReports.util.mParseTime(lOfficeTimings.shiftStart) || SHIFT_START;
					SHIFT_END = cReports.util.mParseTime(lOfficeTimings.shiftEnd) || SHIFT_END;
					BREAK_START = cReports.util.mParseTime(lOfficeTimings.breakStart) || BREAK_START;
					BREAK_END = cReports.util.mParseTime(lOfficeTimings.breakEnd) || BREAK_END;
					
					
				}*/
				
				if (pOptions.dateFormat) {
					DATE_FORMAT = pOptions.dateFormat || DATE_FORMAT;
				}
				
				/**
				 * Registers and stores the request details into the stack, 
				 * so that it can be popped out on 'back' button event
				 */  
				if (pOptions.details) {
					this.util.REQUEST_DETAILS.push(pOptions.details);
				}
			}
			
			//cReports.util.WORKING_HOURS = WORKING_HOURS;
			cReports.util.HALF_DAY_REQ_HOURS = HALF_DAY_REQ_HOURS;
			cReports.util.FULL_DAY_REQ_HOURS = FULL_DAY_REQ_HOURS;
			cReports.util.SHIFT_START = SHIFT_START;
			cReports.util.SHIFT_END = SHIFT_END;
			cReports.util.BREAK_START = BREAK_START;
			cReports.util.BREAK_END = BREAK_END;
			
			cReports.util.DATE_FORMAT = DATE_FORMAT;
			
			var lShiftEndTime = (SHIFT_END.hours * (1000 * 60 * 60) + SHIFT_END.minutes * (1000*60)) - (SHIFT_START.hours * (1000 * 60 * 60) + SHIFT_START.minutes * (1000*60));
			cReports.util.WORKING_HOURS = lShiftEndTime/ cReports.util.HOURS_SCALE;
			
			//this.users.mStoreUsers(pUsersList);
			
		},
		mStoreUsers: function(pUsersList) {
			this.users.mStoreUsers(pUsersList);
		},
		/**
		 * Generates Team Series Report. 
		 * The report will render components like chart, grid based on request for components. 
		 *
		 * @param	pDate			string 			Since team report will be generated for selected date(single), this field will hold selected date value in the format - ‘MMM dd, yy'
		 * @param 	pComponents		List<Object> 	Consists of components need to be rendered along with the extra params needed for the same
		 */
		mGetTeamReport: function(pDate, pComponents, pInitOptions, pUsersList) {
			
			var XAXIS_TITLE = 'Employees';
			var CHART_TYPE = 'column';
			
			var GRID_SORTABLE = true;
			var GRID_PAGINATION = true;
			var GRID_SEARCH = true;
			var GRID_HEIGHT = 170;
			var GRID_REORDER = false;
			var GRID_SCROLLABLE = true;
			
			$.each(pComponents, function(i, pComponent) {
				if (pComponent.type === 'chart' && pComponent.options) {
					
					if (!pComponent.options.chartType) {
						pComponent.options.chartType = CHART_TYPE;
					}
					
					if (!pComponent.options.chartOptions) {
						pComponent.options = {
								chartOptions: {},
								chartType: CHART_TYPE	
						};
					}
					if (!(pComponent.options.chartOptions.xAxis && pComponent.options.chartOptions.xAxis.title)) {
						pComponent.options.chartOptions.xAxis = {
								title: {
									text: XAXIS_TITLE
								}
						};
					}
				}
				
				if (pComponent.type === 'grid' && pComponent.options) {
					if (!pComponent.options.gridOptions) {
						pComponent.options = {
							gridOptions: {}	
						};
					}
					if (pComponent.options.gridOptions.sortable == undefined) {
						pComponent.options.gridOptions.sortable = GRID_SORTABLE;
					}
					if (pComponent.options.gridOptions.pagination == undefined) {
						pComponent.options.gridOptions.pagination = GRID_PAGINATION;
					}
					if (pComponent.options.gridOptions.search == undefined) {
						pComponent.options.gridOptions.search = GRID_SEARCH;
					}
					if (pComponent.options.gridOptions.height == undefined) {
						pComponent.options.gridOptions.height = GRID_HEIGHT;
					}
					if (pComponent.options.gridOptions.columnReorder == undefined) {
						pComponent.options.gridOptions.columnReorder = GRID_REORDER;
					}
					if (pComponent.options.gridOptions.scrollable == undefined) {
						pComponent.options.gridOptions.scrollable = GRID_SCROLLABLE;
					}
					
				}

			});
			
			var COMPONENTS = [{
				type: 'chart',
				id: 'body',
				options: {
					chartType: 'column',
					chartOptions: {}
				}
			}];
			
			COMPONENTS = pComponents || COMPONENTS;
			
			this.users.mStoreUsers(pUsersList);
			
			
			var lOfficeDetails = cReports.util.mIsValidArrayObject(pInitOptions.officeDetails, 'officeDetails');
			
			var lHolidaysDetails = cReports.util.mIsValidArrayObject(pInitOptions.holidaysDetails, 'holidayDetails');
			
			var lFrequency = pInitOptions.details.frequency;
			
			this.http
				.mGetSubordinatesAttendance(pDate, lFrequency, pUsersList, lOfficeDetails, lHolidaysDetails)
				.then(function(lResult) {
	
					if (lResult.success) {
						pInitOptions.officeTimings = lResult.officeTimings;
						cReports.init(pInitOptions, pUsersList);
						
						
						cReports.util.mIsValidArrayObject(COMPONENTS, 'component');
						$.each(COMPONENTS, function(i, lComponent) {
							cReports.mRenderComponent(lComponent.type, lComponent.id, lResult, lComponent.options);
						});
						
						
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': true,
							'requestDetails': pInitOptions.details,
							'response': lResult,
							'event': this
						}});
						document.dispatchEvent(lEvent);	
					} else {
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': false,
							'event': this,
							'result': lResult
						}});
						document.dispatchEvent(lEvent);
						throw JSON.stringify(lResult);
					}
					
					
				}, this.mErrorHandler);
		
		},
		/**
		 * Generates Timeline Gantt Report. 
		 * The report will render components like chart based on request for components. 
		 * 
		 * @param	pDate			string 			Since Timeline report will be generated for selected date(single), this field will hold selected date value in the format - ‘MMM dd, yy'
		 * @param 	pComponents		List<Object> 	Consists of components need to be rendered along with the extra params needed for the same
		 * @param 	pUserId			string			User Id for whom details need to be fetched
		 */
		mGetTimelineReport: function(pDate, pComponents, pInitOptions, pUsersList, pUserId) {
			
			
			var XAXIS_TITLE = 'Employees';
			var CHART_TYPE = 'xrange';

			$.each(pComponents, function(i, pComponent) {
				if (pComponent.type === 'chart' && pComponent.options) {
					
					if (!pComponent.options.chartType) {
						pComponent.options.chartType = CHART_TYPE;
					}
					/*if (!pComponent.options.chartOptions) {
						pComponent.options = {
								chartOptions: {}	
						};
					}
					if (!(pComponent.options.chartOptions.xAxis && pComponent.options.chartOptions.xAxis.title)) {
						pComponent.options.chartOptions.xAxis = {
								title: {
									text: XAXIS_TITLE
								}
						};
					}*/
				}

			});
			
			
			var COMPONENTS = [{
				type: 'chart',
				id: 'body',
				options: {
					chartType: 'column',
					chartOptions: {}
				}
			}];
			
			COMPONENTS = pComponents || COMPONENTS;
			
			//this.init(pInitOptions, pUsersList);
			this.users.mStoreUsers(pUsersList);
			
			var lOfficeDetails = cReports.util.mIsValidArrayObject(pInitOptions.officeDetails, 'officeDetails');
			
			var lFrequency = pInitOptions.details.frequency;
			
			this.http
				.mGetTimelineAttendance(pUserId, pDate, lFrequency, pUsersList, lOfficeDetails)
				.then(function(lResult) {
					if (lResult.success) {
						pInitOptions.officeTimings = lResult.officeTimings;
						cReports.init(pInitOptions, pUsersList);
						
						cReports.util.mIsValidArrayObject(COMPONENTS, 'component');
						$.each(COMPONENTS, function(i, lComponent) {
							cReports.mRenderComponent(lComponent.type, lComponent.id, lResult, lComponent.options);
						});
						
						
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': true,
							'requestDetails': pInitOptions.details,
							'response': lResult,
							'event': this
						}});
						document.dispatchEvent(lEvent);	
					} else {
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': false,
							'event': this,
							'result': lResult
						}});
						document.dispatchEvent(lEvent);
						throw JSON.stringify(lResult);
					}
					
				}, this.mErrorHandler);
		
		},
		/**
		 * Generates User Periodic Report. 
		 * The report will render components like chart based on request for components. 
		 * 
		 * @param 	pUser			Object			User obj whose details need to be fetched
		 * @param	pFromDate	    string 			Begin Date for which details need to be fetched – format: ‘MMM dd, yy'
		 * @param	pToDate	        string 			End Date for which details need to be fetched – format: ‘MMM dd, yy'
		 * @param 	pComponents		List<Object> 	Consists of components need to be rendered along with the extra params needed for the same
		 */
		mGetUserReport: function(pUser, pFromDate, pToDate, pComponents, pInitOptions) {
			
			var lFrequency = pInitOptions.details.frequency;
			var XAXIS_TITLE = lFrequency ? cReports.util.FREQUENCY[lFrequency].axis : undefined;
			var CHART_TYPE = 'column';
			var GRID_SORTABLE = false;
			var GRID_PAGINATION = false;
			var GRID_SEARCH = false;
			var GRID_HEIGHT = 170;
			var GRID_REORDER = false;
			var GRID_SCROLLABLE = false;
			
			$.each(pComponents, function(i, pComponent) {
				if (pComponent.type === 'chart' && pComponent.options) {
					
					if (!pComponent.options.chartType) {
						pComponent.options.chartType = CHART_TYPE;
					}
					
					if (!pComponent.options.chartOptions) {
						pComponent.options = {
								chartOptions: {},
								chartType: CHART_TYPE
						};
					}
					if (!(pComponent.options.chartOptions.xAxis && pComponent.options.chartOptions.xAxis.title)) {
						pComponent.options.chartOptions.xAxis = {
								title: {
									text: XAXIS_TITLE
								}
						};
					}
				}
				
				if (pComponent.type === 'grid') {
					if (!pComponent.options.gridOptions) {
						pComponent.options = {
							gridOptions: {}	
						};
					}
					
					if (pComponent.options.gridOptions.sortable == undefined) {
						pComponent.options.gridOptions.sortable = GRID_SORTABLE;
					}
					if (pComponent.options.gridOptions.pagination == undefined) {
						pComponent.options.gridOptions.pagination = GRID_PAGINATION;
					}
					if (pComponent.options.gridOptions.search == undefined) {
						pComponent.options.gridOptions.search = GRID_SEARCH;
					}
					if (pComponent.options.gridOptions.height == undefined) {
						pComponent.options.gridOptions.height = GRID_HEIGHT;
					}
					if (pComponent.options.gridOptions.columnReorder == undefined) {
						pComponent.options.gridOptions.columnReorder = GRID_REORDER;
					}
					if (pComponent.options.gridOptions.scrollable == undefined) {
						pComponent.options.gridOptions.scrollable = GRID_SCROLLABLE;
					}
				}
			});
			var COMPONENTS = [{
				type: 'chart',
				id: 'body',
				options: {
					chartType: 'column',
					chartOptions: {}
				}
			}];
			
			COMPONENTS = pComponents || COMPONENTS;

			//this.init(pInitOptions, pUsersList);
			this.users.mStoreUsers(pUser);
			
			var lOfficeDetails = cReports.util.mIsValidArrayObject(pInitOptions.officeDetails, 'officeDetails');
			
			var lHolidaysDetails = cReports.util.mIsValidArrayObject(pInitOptions.holidaysDetails, 'holidayDetails');
			
			
			if (pUser) {
				this.http
				.mGetUserAttendance(pUser, pFromDate, pToDate, lFrequency, lOfficeDetails, lHolidaysDetails)
				.then(function(lResult) {
					if (lResult.success) {
						pInitOptions.officeTimings = lResult.officeTimings;
						cReports.init(pInitOptions, pUser);
						
						cReports.util.mIsValidArrayObject(COMPONENTS, 'component');
						$.each(COMPONENTS, function(i, lComponent) {
							cReports.mRenderComponent(lComponent.type, lComponent.id, lResult, lComponent.options);
						});
						
						
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': true,
							'requestDetails': pInitOptions.details,
							'response': lResult,
							'event': this
						}});
						document.dispatchEvent(lEvent);	
					} else {
						var lEvent = new CustomEvent('requestcomplete', {detail: { 
							'success': false,
							'event': this,
							'result': lResult
						}});
						document.dispatchEvent(lEvent);	
						throw JSON.stringify(lResult);
					}
					
				}, this.mErrorHandler);
			} else {
				var lEvent = new CustomEvent('requestcomplete', {detail: { 
					'success': false,
					'event': this
				}});
				document.dispatchEvent(lEvent);	
				throw JSON.stringify({message: 'User is not available'});
			}
		},
		mErrorHandler: function(e) {
			var lEvent = new CustomEvent('requestcomplete', {detail: { 
				'success': false,
				'event': e
			}});
			document.dispatchEvent(lEvent);	
		},
		mRenderComponent: function(pType, pDivId, pData, pComponentOptions) {
			switch(pType) {
			
				case 'chart':
					var lChartType = pComponentOptions.chartType;
					var lIsPacked = pComponentOptions.isPacked;
					var lFrequency = pData.frequency;
					var lChartOptions = pComponentOptions.chartOptions;
					return this.charts.mDrawChart(pDivId, lChartOptions, pData, lChartType, lIsPacked, lFrequency);
					break;
				case 'grid':
					var lFrequency = pData.frequency;
					var lGridOptions = pComponentOptions.gridOptions;
					return this.grid.mRenderAttendance(pDivId, pData, lFrequency, lGridOptions);
					break;
				case 'summary':
					var lFrequency = pData.frequency;
					var lSummaryOptions = pComponentOptions.summaryOptions;
					var lSummaryData = this.grid.mBuildFrequencyData(pData, lFrequency);
					return this.grid.mRenderSummary(pDivId, lSummaryData.summaryTableData, lSummaryOptions);
					break;
				default:
					throw 'Invalid Component Type. Allowed types are - "chart" or "grid" or "summary"';
			}
		},
		users: {
			USERS: {},
			/**
			 * Gets User based on userId.
			 * @param pUserId	user id for whose User object needs to be fetched
			 * @returns User Object
			 */
			mGetUserById: function(pUserId) {
				var lMatchedUser = {};
				$.each(this.USERS, function(i, lUser) {
					if(lUser.userId === pUserId) {
						lMatchedUser = lUser;
					}				
				});
				return lMatchedUser;
			},
			
			/**
			 * Gets User based on name.
			 * @param pName	user name for whose User object needs to be fetched
			 * @returns User Object
			 */
			mGetUserByName: function(pName) {
				var lMatchedUser = {};
				$.each(this.USERS, function(i, lUser) {
					if(lUser.name === pName) {
						lMatchedUser = lUser;
					}				
				});
				return lMatchedUser;
			},
			/**
			 * Stores the List<User> into static member - USERS
			 * Currently the list is sorted in ascending order and by key 'name'.
			 * This method must be called once the Users List is fetched from server.
			 * Acts as a cache for Users.
			 * @param pUserList		List<User>	 Users List
			 */
			mStoreUsers: function(pUserList) {
				if (pUserList) {
					if (!(pUserList instanceof Array)) {
						pUserList = [pUserList];
					}
					cReports.util.mIsValidArrayObject(pUserList, 'user');
					this.USERS = {};	
					var index = undefined;
					$.each(pUserList, function(i, lUser) {
						if (lUser.userId === '70008') {
							index = i;
						}
					});
					if (index > -1) {
						pUserList.splice(index, 1);
					}
						
					this.USERS = cReports.util.mSort(pUserList, 'asc', 'name');
				}
				
			}
		},
		/**
		 * Utilities for chart, grid and users APIs
		 * 
		 * @chg kcr 2016-04-14		Added legend details for Short Leave and Official Out
		 */
		util: {
			MONTH_NAMES : ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
			FREQUENCY : { //Period for which graph or grid components to be rendered	
				'yearly': {
					title: 'Yearly', 
					axis: 'Year', 
					drill: 'monthly',
					hasDrilldown: true
				},
				'monthly': {
					title: 'Monthly', 
					axis: 'Month', 
					drill: 'weekly',
					hasDrilldown: true
				}, 
				'weekly': {
					title: 'Weekly', 
					axis: 'Week', 
					drill: 'daily',
					hasDrilldown: true
				}, 
				'daily': {
					title: 'Daily', 
					axis: 'Date',
					drill: 'daily',
					hasDrilldown: false
				}
			},
			FILTER: {
				'yearly': {params: ['year']},
				'monthly': {params: ['year', 'month']}, 
				'weekly': {params: ['year', 'week']}, 
				'daily': {params: ['date']},
			},
			LEGEND_DETAILS: { 
				shortage: {
					color: 'red', 
					title: 'Effective Hours Shortage',
					visible: true
				},
				excess: {
					color: 'darkgreen',
					title: 'Effective Hours Extra',
					visible: true
				},
				onDuty: {
					color: 'rgb(197, 212, 76)', 
					title: 'On Duty Hours',
					text: 'On Duty',
					visible: true
				},
				effectiveHours: {
					color: '#91CC03', 
					title: 'Effective Hours',
					text: '',
					visible: true
				},
				leaves: {
					color: 'burlywood', 
					title: 'Leaves',
					text: 'On Leave',
					visible: true
				},
				tour: {
					color: '#9A7DCA', 
					title: 'Tour',
					text: 'On Tour',
					visible: true
				},
				officialIn: {
					color: '#FFC3DC',  // Light Crimson
					title: 'Official In',
					text: 'Official In',
					visible: true
				},
				officialOut: {
					color: '#FFC3DC',  // Light Crimson
					title: 'Official Out',
					text: 'Official Out',
					visible: true
				},
				shortLeaveIn: {
					color: '#FFD881',  // Light Orange
					title: 'Short Leave In',
					text: 'Short Leave In',
					visible: true
				},
				shortLeaveOut: {
					color: '#FFD881',  // Light Orange
					title: 'Short Leave Out',
					text: 'Short Leave Out',
					visible: true
				},
				present: {
					color: '#7cb5ec',
					text: ''
				},
				breakTime: {
					color: '#FFFFFF', 
					title: 'Break Time',
					text: ''
				},
				monthly: {
					excess: {
						color: 'darkgreen',
						title: 'Excess'
					},
					shortage: {
						color: 'red', 
						title: 'Shortage'
					}
				},
				weekly: {
					excess: {
						color: 'darkgreen',
						title: 'Excess'
					},
					shortage: {
						color: 'red', 
						title: 'Shortage'
					}
				},
				daily: {
					excess: {
						color: 'darkgreen',
						title: 'Excess'
					},
					shortage: {
						color: 'red', 
						title: 'Shortage'
					}
				}	
			},
			ZONE: [{
				entryExit: 'entry',
				area: 'ground floor',
				zone: 'Design Area ( Ground Floor )'
			},{
				entryExit: 'entry',
				area: 'first floor',
				zone: 'Design Area ( 1st Floor )'
			},{
				entryExit: 'entry',
				area: 'second floor',
				zone: 'Design Area ( 2nd Floor )'
			},{
				entryExit: 'entry',
				area: 'server room',
				zone: 'Design Area ( 2nd Floor )'
			},{
				entryExit: 'exit',
				area: 'server room',
				zone: 'Design Area ( 2nd Floor )'
			},{
				entryExit: 'entry',
				area: 'reception',
				zone: 'Common Area'
			},{
				entryExit: 'entry',
				area: 'terrace',
				zone: 'Common Area'
			},{
				entryExit: 'exit',
				area: 'terrace',
				zone: 'Terrace'
			},{
				entryExit: 'exit',
				area: 'ground floor',
				zone: 'Common Area'
			},{
				entryExit: 'exit',
				area: 'first floor',
				zone: 'Common Area'
			},{
				entryExit: 'exit',
				area: 'second floor',
				zone: 'Common Area'
			},{
				entryExit: 'exit',
				area: 'reception',
				zone: 'Out of Office'
			},{
				entryExit: 'entry',
				area: 'pune back door',
				zone: 'Common Area'
			},{
				entryExit: 'entry',
				area: 'pune reception 1',
				zone: 'Design Area'
			},{
				entryExit: 'exit',
				area: 'pune reception 1',
				zone: 'Out of Office'
			},{
				entryExit: 'exit',
				area: 'pune back door',
				zone: 'Design Area'
			},{
				entryExit: 'entry',
				area: 'default',
				zone: 'In Office'
			},{
				entryExit: 'exit',
				area: 'default',
				zone: 'Out of Office'
			}],
			HOURS_SCALE: (1000 * 60 * 60),
			DEFAULT_ZONE: 'Office',
			WEEK_PREFIX: 'WK-',
			WEEK_YEAR_DELIM: ', ',
			REQUEST_DETAILS: [],
			mSort: function(pArray, pOrder, pKey) {
				var ORDER = 'desc' || pOrder;
				var KEY = '' || pKey;
				var SIGN = ORDER === 'desc' ? 1 : -1;
				
				pArray.sort(function(a, b) {
					var lObjOne = a;
					var lObjTwo = b;
					if (KEY !== '') {
						lObjOne = a[KEY];
						lObjTwo = b[KEY];
					}
					
					if (typeof(lObjOne) === 'string' &&  typeof(lObjTwo) === 'string') {
						if (lObjOne > lObjTwo) return (1 * SIGN);						
						if (lObjOne < lObjTwo) return (-1 * SIGN);	
						return 0;
					} else {
						return SIGN * (lObjOne - lObjTwo);
					}
				});
				
				return pArray;
			},
			mGetCurrentRequestDetails: function() {
				return this.REQUEST_DETAILS[this.REQUEST_DETAILS.length - 1];
			},
			mGetOfficeDetailsByLocation: function(pLocationId, pOfficeDetailsList) {
				var lOfficeDetails = null;
				for (var i in pOfficeDetailsList) {
					lOfficeDetails = pOfficeDetailsList[i];
					if (lOfficeDetails.locationId == pLocationId) {
						return lOfficeDetails;
					}
				}
				return lOfficeDetails;
			},
			mExtractOfficeDetails: function(pRawOfficeDetails) {
				var HALF_DAY_REQ_HOURS = cReports.util.mParseTime(pRawOfficeDetails.requiredHoursForHalfDay);
				var FULL_DAY_REQ_HOURS = cReports.util.mParseTime(pRawOfficeDetails.requiredHoursForFullDay);
				var SHIFT_START = cReports.util.mParseTime(pRawOfficeDetails.shiftStart);
				var SHIFT_END = cReports.util.mParseTime(pRawOfficeDetails.shiftEnd);
				var BREAK_START = cReports.util.mParseTime(pRawOfficeDetails.breakStart);
				var BREAK_END = cReports.util.mParseTime(pRawOfficeDetails.breakEnd);
				
				var WorkTimeMillis = (SHIFT_END.toMillis() - SHIFT_START.toMillis());

				var WORKING_HOURS = WorkTimeMillis / cReports.util.HOURS_SCALE;
				
				var lOfficeDetails = {
						workingHours: WORKING_HOURS,
						requiredHoursForHalfDay: HALF_DAY_REQ_HOURS,
						requiredHoursForFullDay: FULL_DAY_REQ_HOURS,
						shiftStart: SHIFT_START,
						shiftEnd: SHIFT_END,
						breakStart: BREAK_START,
						breakEnd: BREAK_END
				};
				return lOfficeDetails;
			},
			/**
			 * Triggers event 'requestreload' with request details of previous state.
			 * Basically, on on adding event listener to DOM of the application, 
			 * we will get request details(like userId, beginDate, endDate, frequency etc)  of previous state
			 * 
			 * usage: call the method - 'cReports.util.mGoBack()' to trigger event
			 */
			mGoBack: function() {
				if (this.REQUEST_DETAILS.length > 1) {
					this.REQUEST_DETAILS.splice(-1,1);
					var lRequestDetails = this.REQUEST_DETAILS.pop();
					var lEvent = new CustomEvent('requestreload', {detail: lRequestDetails});
					document.dispatchEvent(lEvent);	
				}
			},
			/**
			 * Triggers the event 'requestreload' on clicking on graph data point, 
			 * and hence the event can be captured to drill down to next frequency or 
			 * period from current period
			 * 
			 * @param pUserId
			 * @param pFrequency
			 * @param pBeginDate
			 * @param pEndDate
			 */
			mDrillDown: function(pUserId, pFrequency, pBeginDate, pEndDate, pForSubordinates) {
				//if(pFrequency.toLowerCase() !== 'hourly') {
					var lBeginDate = pBeginDate > 0 ? new Date(pBeginDate) : new Date(pBeginDate); // to check whether its a number or string
					var lEndDate = pEndDate > 0 ? new Date(pEndDate) : new Date(pEndDate);
					var lEvent = new CustomEvent('requestreload', {detail: { 
						'frequency': pFrequency,
						'beginDate': lBeginDate,
						'endDate': lEndDate,
						'userId': pUserId,
						'subordinates': pForSubordinates
					}});
					document.dispatchEvent(lEvent);	
				//}
			},
			/**
			 * Parses the time in the format 'HH:mm' into an object of time obj = { hours: HH, minutes: mm }
			 * @param pTime
			 * @returns { hours: HH, minutes: mm }
			 */
			mParseTime: function(pTime) {
				var lTimeArr = pTime.split(':');
				var lTimeObj = {
					hours: parseInt(lTimeArr[0]),
					minutes: parseInt(lTimeArr[1]),
					toMillis: function(){
						return (this.hours * (1000 * 60 * 60) + this.minutes * (1000*60));
					},
					toHours: function() {
						return this.toMillis()/ cReports.util.HOURS_SCALE;
					}
				};
				return lTimeObj;
			},
			/**
			 * A float number will be formatted to string to suit the time format i.e. (HH:mm)
			 * @param pNumber
			 * @param pRetainSign
			 * @returns {String}
			 */
			mFormatNumber: function(pNumber, pRetainSign) {
				pNumber = parseFloat(pNumber);
			    var lIsNegative = pNumber >= 0 ? false : true;
				var lMinutes = /*pNumber > 0 ? Math.ceil((Math.abs(pNumber%1) * 60)) : */Math.round((Math.abs(pNumber%1) * 60));		    
				pNumber = Math.abs(pNumber);
				var lHours = pNumber > 0 ? Math.floor(pNumber) : Math.ceil(pNumber);
				if (lMinutes >= 60) {
			    	lMinutes = 0;
			    	lHours = lHours + 1;
			    }
				if (lIsNegative && pRetainSign) {
					lHours = '-' + lHours;
				}
				return lHours + ':' + this.mFormatFraction(lMinutes);
				
			},
			/**
			 * Fraction number is converted to string such that 
			 * it prepends '0' to the number if the number is < 10
			 * @param pFractionNumber
			 * @returns
			 */
			mFormatFraction: function(pFractionNumber) {
				if (pFractionNumber <= 9) {
					return '0' + pFractionNumber;
				}
				return pFractionNumber;
			},
			mIsValidObject: function(pObjFields, pModelFields) {
				var lIsValid = true;
				$.each(pModelFields, function(i, lField) {
					if (pObjFields.indexOf(lField.datafield) == -1){
						lIsValid = false;
					}
				});
				return lIsValid;
			},
			mIsValidArrayObject: function(pArray, pObjectType) {
				var vm = this;
				var lIsValid = true;
				if (pArray instanceof Array) {
					$.each(pArray, function(i, pCurObj) {
						var lObjFields = Object.keys(pCurObj);
						
						switch(pObjectType) {
							
							case 'user':
								lIsValid = vm.mIsValidObject(lObjFields, cReports.models.user);
								
								if (!lIsValid) {
									throw 'Invalid user object format. The object should have following fields: \n ' + cReports.models.toString(cReports.models.user);
								}
								break;
							case 'component':
								lIsValid = vm.mIsValidObject(lObjFields, cReports.models.component);
								if (!lIsValid) {
									throw 'Invalid component object format. The object should have following fields: \n ' + cReports.models.toString(cReports.models.component);
								}
								break;
							case 'holidayDetails':
								lIsValid = vm.mIsValidObject(lObjFields, cReports.models.holidayDetails);
								if (!lIsValid) {
									throw 'Invalid holidayDetails object format. The object should have following fields: \n ' + cReports.models.toString(cReports.models.holidayDetails);
								}
								var lHolidaysObjFields = Object.keys(pCurObj.holidaysList);
								lIsValid = vm.mIsValidObject(lHolidaysObjFields, cReports.models.holidayDetails[1].fields);
								break;
							case 'officeDetails':
								lIsValid = vm.mIsValidObject(lObjFields, cReports.models.officeDetails);
								if (!lIsValid) {
									throw 'Invalid officeDetails object format. The object should have following fields: \n ' + cReports.models.toString(cReports.models.officeDetails);
								}
								break;
							default:
								console.error('Invalid model type supplied');
								
						}
					});
				} else {
					var lObjFields = Object.keys(pArray);
					switch(pObjectType) {
						case 'officeDetails':
							lIsValid = vm.mIsValidObject(lObjFields, cReports.models.officeDetails);
							if (!lIsValid) {
								throw 'Invalid officeDetails object format. The object should have following fields: \n ' + cReports.models.toString(cReports.models.officeDetails);
							}
							break;
						default:
							console.error('Invalid model type supplied');
							
					}
				}
				
				return pArray;
			}
		},
		
		/**
		 *  Http requests to get attendance details
		 */
		http: {
			mGetSubordinatesAttendance: function(pDate, pFrequency, pUsersList, pOfficeDetails, pHolidaysDetails) {
				var lUserIDs = [];
				$.each(pUsersList, function(i, lUser) {
					lUserIDs.push(lUser.userId);
				});
				return $.ajax({
					//url: config.endPoint + 'api/v1/attendance/subordinates/main_swipes/' + pUserId + '/' + pDate ,
					//url: config.endPoint + 'api/v1/users/subordinates/' + pUserId ,
					url: config.endPoint + 'api/v1/attendance/subordinates/' + pDate ,
					type: 'POST',
					data: JSON.stringify({ usersList: pUsersList, officeDetails: pOfficeDetails, holidaysDetails: pHolidaysDetails }),
					contentType: 'application/x-www-form-urlencoded;charset=UTF-8',
					dataType: 'json'
				});
			},
			mGetUserAttendance: function(pUser, pFromDate, pToDate, pFrequency, pOfficeDetails, pHolidaysDetails) {
				return $.ajax({
					url: config.endPoint + 'api/v1/attendance/' + pUser.userId + '/' + pFromDate + '/' + pToDate + '/' + pFrequency,
					type: 'POST',
					data: JSON.stringify({ officeDetails: pOfficeDetails, holidaysDetails: pHolidaysDetails, user: pUser }),
					dataType: 'json'
				});
			},
			mGetTimelineAttendance: function(pUserId, pDate, pFrequency, pUsersList, pOfficeDetails) {
				var lUserIDs = [];
				$.each(pUsersList, function(i, lUser) {
					if (lUser.userId == pUserId) {
						pUsersList[i].baseUser = true;
					} else {
						pUsersList[i].baseUser = false;
					}
				});
				return $.ajax({
					//url: config.endPoint + 'api/v1/attendance/subordinates/main_swipes/' + pUserId + '/' + pDate ,
					//url: config.endPoint + 'api/v1/users/subordinates/' + pUserId ,
					url: config.endPoint + 'api/v1/attendance/subordinates/main_swipes/' + pDate,
					type: 'POST',
					data: JSON.stringify({ usersList: pUsersList, officeDetails: pOfficeDetails  }),
					contentType: 'application/x-www-form-urlencoded;charset=UTF-8',
					dataType: 'json'
				});
			}
		},
		
		/**
		 * APIs for chart component
		 */
		charts: {
			TYPES: ['column', 'bar', 'series', 'area', 'xrange'],
			chartObj: {},
			/**
			 * Delegates to respective Chart Renderers
			 */
			mDrawChart : function(pDivID, pOptions, pAttendanceList, pChartType, pIsPacked, pFrequency) {
				if (pFrequency) {
					pFrequency = pFrequency.toLowerCase();
				}
				
				this.attendanceList = pAttendanceList.userAttendance;
				switch(pChartType) {
					case 'column':
					case 'bar':
					case 'area':
					case 'series':
						return this.mDrawSeriesChart(pDivID, pOptions, pAttendanceList, pChartType, pFrequency);
						break;
					case 'xrange': 
						return this.mDrawTimelineChart(pDivID, pOptions, pAttendanceList, pChartType, pIsPacked, pFrequency);
						break;
					default: 
						throw 'Invalid chart type. Allowed types are - "column" or "bar" or "xrange"';
				}
			},
			/**
			 * Delegates to respective Chart Data Build handlers
			 */
			mBuildChartData : function(pAttendanceList, pChartType, pFrequency) {
				switch(pChartType) {
					case 'column':
					case 'bar':
					case 'area':
					case 'series':
						return this.mBuildSeriesChartData(pAttendanceList, pFrequency);
						break;
					case 'xrange': 
						return this.mBuildTimelineChartData(pAttendanceList, pFrequency);
						break;
					default: 
						throw 'Invalid chart type. Allowed types are - "column" or "bar" or "xrange"';
				}
			},
			/**
			 * Date string is converted into 
			 * an object of {year: Int, month: Int, week: Int, date: Date} based on frequency 
			 * @param pDateExpr
			 * @returns {year: Int, month: Int, week: Int, date: Date}
			 */
			mGetMatchedDataByDate: function(pDateExpr) {	
				var lDateObj = {};
	            var lDailyAttendanceData = this.attendanceList;
				var lFrequency = lDailyAttendanceData[0].frequency.toLowerCase();
				if(lFrequency == 'weekly') {
					lDateObj = {
							year: parseInt(pDateExpr.split(', ')[1]),
							week: parseInt(pDateExpr.split(', ')[0].split('-')[1]),
					};				
				} else {
					if (! (pDateExpr instanceof Date)) {
						pDateExpr = new Date(pDateExpr);
		            }
					lDateObj = {
							year: pDateExpr.getFullYear(),
							month: pDateExpr.getMonth()+1,
							date: pDateExpr
					};
				}
				var lFilterParams = cReports.util.FILTER[lFrequency].params;
				var lIsMatchedData = false;
				var lMatchedData = {};
	            $.each(lDailyAttendanceData, function(index, lAttendanceData) {
	            	lIsMatchedData = lFilterParams.every(function(param) {
	            		return lDateObj[param] == lAttendanceData[param];
	            	});
	            	if(lIsMatchedData) {
	            		lMatchedData = lAttendanceData;
	            	}
	            });           
	            return lMatchedData;       
			},
			/**
			 * Handler for chart's datapoint is click.
			 * Extracts the attendance details object and calls drillDown method (in turn triggers 'requestreload' event)
			 * @param pChartDiv
			 * @param pOpts
			 */
			mDatapointSelectListener: function(pChartDiv, pOpts) {
				var lAttendanceDetails = pOpts.series.chart.series[5].data[pOpts.index]; // Attendance details at column 6
				lMatchedData = lAttendanceDetails;
		        var FREQUENCY = lAttendanceDetails.frequency || 'daily';
		        cReports.util.mDrillDown(lAttendanceDetails.userId, 
		        						 cReports.util.FREQUENCY[FREQUENCY].drill, 
		        						 lAttendanceDetails.beginDate, 
		        						 lAttendanceDetails.endDate);
			},
			/**
			 * Converts Data into daily data and hence pushes the data as series data format of Highcharts.
			 * @param pAttendanceList	
			 * @param pChartSeries		Array	Predefined chart series arrays in which the converted data is pushed.
			 * 									These arrays are predefined in mBuildSeriesChartData() method
			 * 
			 * @chg kcr 2016-04-14		Modified lEffectiveHours to set value to the graph only if the value is positive. 
			 * 							Fixes bug that displays effective hours part at negative part
			 * 
			 */
			mGetUsersDailyData: function(pAttendanceList, pChartSeries) {
				var lEmployees =  cReports.users.USERS;
				var lEmployeesAttendance = pAttendanceList.subordinatesAttendance;
				var lEmployeeNames = [];
				var lMaxEffectiveHours = 0;
				var lMinLeaveHours = 0;
				var lChartData = {};
				$.each(lEmployees,
						function(index, lEmployee) {
						    lEmployeeNames.push(lEmployee.name);
							var lAttendanceDetails = lEmployeesAttendance[lEmployee.userId];
							
							var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
							var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
							
							if (lMaxEffectiveHours < lOfficeDetails.workingHours) {
								lMaxEffectiveHours = lOfficeDetails.workingHours;
							}
							if (lAttendanceDetails) {
								
								var lDate = new Date(lAttendanceDetails.date);
								var lOnDuty = lAttendanceDetails.onDuty * lOfficeDetails.workingHours;
								var lTour = lAttendanceDetails.tour * lOfficeDetails.workingHours;
								var lMinEffectiveHours = lAttendanceDetails.requiredHours
														 - (lOnDuty + lTour);
								var lEffectiveHours = lAttendanceDetails.effectiveHours;
								var lEffectiveHoursDifference = (lEffectiveHours - lMinEffectiveHours);
								var lLeavesTaken = -(lAttendanceDetails.leavesTaken) * lOfficeDetails.workingHours;
								var lExcess = 0;
								var lShortage = 0;
								if (lEffectiveHoursDifference >= 0) {
									lExcess = lEffectiveHoursDifference;
									
								} else if (lEffectiveHoursDifference < 0) {
									lEffectiveHoursDifference = -(lEffectiveHoursDifference);
									lMinEffectiveHours = (lMinEffectiveHours - lEffectiveHoursDifference);
									lShortage = lEffectiveHoursDifference;
								}
		
								if (lDate.getTime() >= Date.today().getTime()) {
									pChartSeries[0].borderColor = 'rgba(255,63,63,0.5)';
									pChartSeries[0].color = 'rgba(255,255,255,0.2)';
								}
								if (lExcess > 0) {				
									lEffectiveHours = lMinEffectiveHours;
									if (lExcess > (lTour + lOnDuty)) {
										lExcess = lExcess - (lTour + lOnDuty);
									}
								}
								
								if (lMaxEffectiveHours < lAttendanceDetails.requiredHours +  lExcess + 1) {
									lMaxEffectiveHours = lAttendanceDetails.requiredHours +  lExcess + 1;
								}
								if (lMinLeaveHours > lLeavesTaken) {
									lMinLeaveHours = lLeavesTaken;
								}
								
								lEffectiveHours = lEffectiveHours < 0 ? 0 : lEffectiveHours;
								
								pChartSeries[0].data
									.push(lShortage);
							
							    pChartSeries[1].data
									.push(lExcess);
							    
							    pChartSeries[2].data.push(lOnDuty);
							    
								pChartSeries[3].data
									.push(lEffectiveHours);
								
								pChartSeries[4].data.push(lAttendanceDetails.requiredHours);
								
								pChartSeries[5].data.push(lAttendanceDetails);
								
								pChartSeries[6].data.push(lTour);
								
								pChartSeries[7].data.push(lLeavesTaken);
								
								
							}
						});
				lChartData.extremes = [lMinLeaveHours, lMaxEffectiveHours];
				lChartData.series = pChartSeries;
				lChartData.categories = lEmployeeNames;
				return lChartData;
			},
			mGetDateByFrequency: function(pAttendanceDetails, pFrequency) {
				var lDate;
				switch(pFrequency) {
					case 'monthly': 
						lDate = new Date(pAttendanceDetails.year, pAttendanceDetails.month-1, 1)
									.toString('MMM, yyyy');
						break;
					case 'weekly':
						lDate = cReports.util.WEEK_PREFIX + pAttendanceDetails.week + cReports.util.WEEK_YEAR_DELIM +  pAttendanceDetails.year;
						break;
					case 'daily': 
						lDate = new Date(pAttendanceDetails.date)
								.toString(cReports.util.DATE_FORMAT);
						break;
					default: 
						lDate = new Date(pAttendanceDetails.beginDate)
								.toString(cReports.util.DATE_FORMAT);
				}
				return lDate;
			},
			/**
			 * Converts Data into daily data and hence pushes the data as series data format of Highcharts.
			 * @param pAttendanceList	
			 * @param pChartSeries		Array	Predefined chart series arrays in which the converted data is pushed.
			 * 									These arrays are predefined in mBuildSeriesChartData() method
			 * @param pFrequency		string	Period or Frequency for which chart needs to be rendered. Possible values are - 'monthly' or 'weekly' or 'daily'
			 * 
			 * @chg kcr 2016-04-14		Modified lEffectiveHours to set value to the graph only if the value is positive. 
			 * 							Fixes bug that displays effective hours part at negative part
			 * 
			 */
			mGetUserFrequencyData: function(pAttendanceList, pChartSeries, pFrequency) {
				var lAttendanceDetailsList = pAttendanceList.userAttendance;
				var lDates = [];
				var lMinLeaveHours = 0;
				var lChartData = {};
				if (lAttendanceDetailsList && lAttendanceDetailsList.length > 0) {
					var lEmployee = cReports.users.mGetUserById(lAttendanceDetailsList[0].userId);
					var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
					var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
					var lMaxEffectiveHours = lOfficeDetails.workingHours + 1;
					
					$.each(lAttendanceDetailsList,
							function(index, lAttendanceDetails) {
								if (lAttendanceDetails) {
									
									var lDate = cReports.charts.mGetDateByFrequency(lAttendanceDetails, pFrequency);
									lDates.push(lDate);
									var lOnDuty = lAttendanceDetails.onDuty * lOfficeDetails.workingHours;
									var lTour = lAttendanceDetails.tour * lOfficeDetails.workingHours;
									var lMinEffectiveHours = lAttendanceDetails.requiredHours
															 - (lOnDuty + lTour);
									var lEffectiveHours = lAttendanceDetails.effectiveHours;
									var lEffectiveHoursDifference = (lEffectiveHours - lMinEffectiveHours);
									var lLeavesTaken = -(lAttendanceDetails.leavesTaken) * lOfficeDetails.workingHours;
									var lExcess = 0;
									var lShortage = 0;
									if (lEffectiveHoursDifference > 0) {
										lExcess = lEffectiveHoursDifference;
										
									} else if (lEffectiveHoursDifference < 0) {
										lEffectiveHoursDifference = -(lEffectiveHoursDifference);
										lMinEffectiveHours = (lMinEffectiveHours - lEffectiveHoursDifference);
										lShortage = lEffectiveHoursDifference;
									}
									
									if (lExcess > 0) {			
										lEffectiveHours = lMinEffectiveHours;
										lEffectiveHours = lMinEffectiveHours;
										if (lExcess > (lTour + lOnDuty)) {
											lExcess = lExcess - (lTour + lOnDuty);
										}
									}
		
									if (lMaxEffectiveHours < lAttendanceDetails.requiredHours  + lExcess + 1) {
										lMaxEffectiveHours = lAttendanceDetails.requiredHours  + lExcess + 1;
									}
									
									if (lMinLeaveHours > lLeavesTaken) {
										lMinLeaveHours = lLeavesTaken;
									}
									
									lEffectiveHours = lEffectiveHours < 0 ? 0 : lEffectiveHours;
								
									
									pChartSeries[0].data.push(lShortage);
								
								    pChartSeries[1].data.push(lExcess);
								    
								    pChartSeries[2].data.push(lOnDuty);
								    
									pChartSeries[3].data.push(lEffectiveHours);
			
									
									
									pChartSeries[4].data.push(lAttendanceDetails.requiredHours);
									
									pChartSeries[5].data.push(lAttendanceDetails);
									
									pChartSeries[6].data.push(lTour);
									
									pChartSeries[7].data.push(lLeavesTaken);
									
	
								}
							});
				
				}
				lChartData.series = pChartSeries;
				lChartData.categories = lDates;
				lChartData.extremes = [lMinLeaveHours, lMaxEffectiveHours];
				return lChartData;
			},
			
			/**
			 * Converts data fetched from API response to data format required by 'series' type of Highcharts
			 */
			mBuildSeriesChartData : function(pAttendanceList, pFrequency) {
	
				var lLegends = cReports.util.LEGEND_DETAILS;
				var lChartSeries = [{
					name : lLegends['shortage'].title, //'Effective Hours Shortage',
					color: lLegends['shortage'].color,
					borderColor: lLegends['shortage'].color,
					data : []
				},{
					name : lLegends['excess'].title,//'Effective Hours Extra',
					color: lLegends['excess'].color,
					borderColor: lLegends['excess'].color,
					data : []
				},{
					name : lLegends['onDuty'].title,//'On Duty Hours',
					color: lLegends['onDuty'].color,//'#E0F155', // Lemon Yellow
					borderColor: lLegends['onDuty'].color,//'#E0F155',
					data : []
				},{
					name : lLegends['effectiveHours'].title,//'Effective Hours',
					color: lLegends['effectiveHours'].color,//'#91CC03', // Light Green
					borderColor: lLegends['effectiveHours'].color,//'#91CC03',
					data : []
				},{
					name : 'Required Hours',
					data : [],
					visible: false,
					showInLegend: false
				},{
					name : 'Details',
					data : [],
					visible: false,
					showInLegend: false
				},{
					name : lLegends['tour'].title,//'Tour',
					color: lLegends['tour'].color,//'#9A7DCA', // Light purple
					borderColor: lLegends['tour'].color,//'#9A7DCA',
					data : []
				},{
					name : lLegends['leaves'].title,//'Leaves',
					color: lLegends['leaves'].color,//'burlywood', // Variant of Brown 
					borderColor: lLegends['leaves'].color,//'burlywood', 
					data : []
				}];
				
				var lChartData = (pFrequency 
				                 && this.mGetUserFrequencyData(pAttendanceList, lChartSeries, pFrequency))
				                 || this.mGetUsersDailyData(pAttendanceList, lChartSeries);
				
				/*if (pFrequency) {
					lChartData = this.mGetUserFrequencyData(pAttendanceList, lChartSeries, pFrequency);
				} else {
					lChartData = this.mGetUsersDailyData(pAttendanceList, lChartSeries);
				}*/
				return lChartData;
			},
			mGetLeaveDetailsHTML: function(pLeaveDetails) {
				var lHtmlContent = '';
				if (pLeaveDetails && pLeaveDetails.length > 0) {
					var lTotalLeaves = 0;
					var lLossOfPay = 0;
					var lTableStyle = 'z-index: 999;border-collapse:collapse;';
					var lColStyle = 'border: 1px solid #ADADAD; padding:4px;';
					lHtmlContent += '<tr><td>';
					
					lHtmlContent += '<table class="leaves-tooltip" '+
					                	'style="' + lTableStyle + '"' +
									'>' + 
						              '<tr>' +
						                '<th style="' + lColStyle + '">Leave Type</th>' +
						                '<th style="' + lColStyle + '">From Date</th>' +
						                '<th style="' + lColStyle + '">To Date</th>' +
						                '<th style="' + lColStyle + '">Days</th>' +
						                '<th style="' + lColStyle + '">Remarks</th>' +
						              '</tr>';
						                
								    
					$.each(pLeaveDetails, function(i, lLeave) {
						lHtmlContent +=  '<tr>' +
											'<td style="' + lColStyle + '">' + lLeave.leaveId + '</td>' +
											'<td style="' + lColStyle + '">' + new Date(lLeave.fromDate).toString(cReports.util.DATE_FORMAT) + '</td>' +
											'<td style="' + lColStyle + '">' + new Date(lLeave.toDate).toString(cReports.util.DATE_FORMAT) + '</td>' +
											'<td style="' + lColStyle + '">' + lLeave.postedDays + '</td>' +
											'<td style="' + lColStyle + '">' + lLeave.remarks + '</td>'+
										  '</tr>';
						lTotalLeaves = lTotalLeaves + lLeave.postedDays;
					});
					
					lHtmlContent +=   '<tr>' +
										'<td colspan="3" style="' + lColStyle + '">Total Leaves</td>' +
										'<td colspan="2" style="' + lColStyle + '">' + lTotalLeaves + '</td>' +
									  '</tr>';
					lHtmlContent +=   '<tr>' +
										'<td colspan="3" style="' + lColStyle + '">Loss of Pay</td>' +
										'<td colspan="2" style="' + lColStyle + '">' + lLossOfPay + '</td>' +
									  '</tr>';
					
					lHtmlContent += '</table>';
					
					
					lHtmlContent += '</td></tr>';
					
					$('.leaves-tooltip').css({
						'z-index': '999',
						'border-collapse': 'collapse'
					});
					
					$('.leaves-tooltip th, .leaves-tooltip td').css({
						'border': '1px solid #ADADAD',
						'padding': '4px'
					});
					
					return lHtmlContent;
				} else {
					return false;
				}
			},
			/**
			 * Returns HTML element for customized tooltip.
			 * Gives details of Attendance for every user.
			 * It is basically of two formats(based on type of datapoint hovered on) - 
			 * 1) For Attendance details - Required Hours, Effective Hours, On Duty, Excess.
			 * 2) For Leave Details - Leave type, From date, To date, Days, Remarks.
			 * @param pChartObj
			 * @returns
			 */
			mFormatSeriesChartTooltip: function(pChartObj) {
				
				var lIndex = pChartObj.point.index;
				var lName = pChartObj.point.category;
				var lAttendanceDetails = pChartObj.series.chart.series[5].data[lIndex];
				
				var lEmployee = cReports.users.mGetUserById(lAttendanceDetails.userId);
				var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
				var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
				
				var lBeginDate = new Date(lAttendanceDetails.beginDate).toString(cReports.util.DATE_FORMAT);
				var lEndDate = new Date(lAttendanceDetails.endDate).toString(cReports.util.DATE_FORMAT);
				var lReqdHours = lAttendanceDetails.requiredHours;
				//var lEffHours = pChartObj.series.chart.series[3].yData[lIndex];
				var lEffHours = lAttendanceDetails.effectiveHours;
				var lOnDuty =  lAttendanceDetails.onDuty * lOfficeDetails.workingHours;
				var lTour = lAttendanceDetails.tour * lOfficeDetails.workingHours;
				//var lExcess = pChartObj.series.chart.series[1].yData[lIndex];
				var lExcessOrShortage = lAttendanceDetails.difference + (lOnDuty + lTour);
				var lDataPointType = pChartObj.series.name;
				//var lExcessOrShortage = 0;
				
				var lRange = '';
				if(lAttendanceDetails.frequency.toLowerCase() !== 'daily') {
					lRange = ' (' + lBeginDate + ' - ' + lEndDate + ')';
				}
				
				var lHtmlContent = '';
				lHtmlContent = '<table>';
				 
				lHtmlContent +=  '<tr>' +
				 				  	'<td colspan=2><b>' + lName + '</b>' + lRange + '</td>' +
				 				 '</tr>';
				lHtmlContent +=  '<tr>' +
								  	'<td>&nbsp</td>' + // For bottom margin
								 '</tr>';
				if (lDataPointType.toLowerCase() === 'leaves') {
					var lLeaveDetails = lAttendanceDetails.leavesDetails;
					if (lLeaveDetails && lLeaveDetails.length > 0) {
						lHtmlContent +=  this.mGetLeaveDetailsHTML(lLeaveDetails);
					} else {
						return;
					}
					
				} else {
					lHtmlContent +=  '<tr>'+
										'<td>Required Hours &nbsp</td><td><b>' + cReports.util.mFormatNumber(lReqdHours, true) + '</b></td>'+
									  '</tr>' +
									  '<tr>' +
										'<td>Effective Hours &nbsp</td><td><b>' + cReports.util.mFormatNumber(lEffHours, true) + '</b></td>'+
									  '</tr>' +
									  '<tr>' +
										'<td>On Duty Hours &nbsp</td><td><b>' + cReports.util.mFormatNumber(lOnDuty, true) + '</b></td>'+
									  '</tr>' +
									  '<tr>' +
										'<td>Tour &nbsp</td><td><b>' + cReports.util.mFormatNumber(lTour, true) + '</b></td>'+
									  '</tr>';
	
					var lColor = 'green';
					if(lExcessOrShortage >= 0) {
						//lExcessOrShortage = lExcess;
						lColor = 'green';
					} else {
						//lExcessOrShortage = -lShortage;
						lColor = 'red';  	
					}
					
					lHtmlContent +=  '<tr>' +
									  	'<td>&nbsp</td>' + // For bottom margin
									 '</tr>';
					lHtmlContent += '<tr>'+
									'<td colspan=2><div style="color:' + lColor + ';border: 1px solid ' + lColor + ';width:50px;margin:auto;text-align:center;"><b>' + cReports.util.mFormatNumber(lExcessOrShortage, true) + '</b></div></td>'+
								 '</tr>';				
				}
				
				lHtmlContent += '</table>';
				
				return lHtmlContent;
			},
			
			/**
			 * Renders the series chart (line, bar, column - stacked) based on options and series data.
			 */
			mDrawSeriesChart: function(pDivID, pOptions, pAttendanceList, pChartType, pFrequency) {
	
				var lChartData = this.mBuildChartData(pAttendanceList, pChartType, pFrequency);
	
				//var FREQUENCY = 'daily';
				//pFrequency = pFrequency || FREQUENCY;
				
				var CHART_TYPE = 'column';
				if (this.TYPES.indexOf(pChartType) > -1) {
					CHART_TYPE = pChartType;
				}
				
				var CHART_HEIGHT; // Not initialized to avoid default value setting for chart
				var CHART_WIDTH;  // Not initialized to avoid default value setting for chart
				var CHART_FONT_FAMILY = 'arial';
				var CHART_ZOOM_TYPE = 'xy';
				var CHART_PANNING_ENABLE = true;
				var CHART_PAN_KEY = 'shift';
				var CHART_TITLE_TEXT = '';
				var TITLE_TEXT = '';
				
				var XAXIS_TITLE_TEXT = pFrequency ? cReports.util.FREQUENCY[pFrequency].axis : undefined;
				var XAXIS_TITLE_FONT_WEIGHT = 'bold';
				
				
				var YAXIS_TITLE_TEXT = 'Effective Hours';
				var YAXIS_TITLE_FONT_WEIGHT = 'bold';
				var YAXIS_TICK_AMOUNT = 6;
				var YAXIS_MIN = lChartData.extremes[0];
				var YAXIS_MAX = lChartData.extremes[1];
	
				var LEGEND_ENABLED = false;
				var LEGEND_ID = 'legend';
				var USE_LEGEND_STYLESHEET = false;
				var LEGEND_ALIGN = 'right';
				var LEGEND_VERTICAL_ALIGN = 'top';
				var LEGEND_LAYOUT = 'vertical';
				
				var TOOLTIP_FOLLOW_POINTER = true;
				var TOOLTIP_BG_COLOR = 'rgba(255, 255, 255, 0.97)';
				var TOOLTIP_BORDER_COLOR = '#9E9E9E'; // light gray
				
				
				
				if (pOptions) {
					
					if (pOptions.chart) {
						CHART_HEIGHT = pOptions.chart.height || CHART_HEIGHT;
						CHART_WIDTH = pOptions.chart.width || CHART_WIDTH;
						if (pOptions.chart.width === 'auto') {
							var lWindowWidth = window.outerWidth - (0.072 * window.outerWidth);
							CHART_WIDTH = lWindowWidth || CHART_WIDTH;
						}
						if (pOptions.chart.height === 'auto') {
							var lWindowHeight = window.outerHeight;
							CHART_HEIGHT = (lWindowHeight + 70) || CHART_HEIGHT;
						}
						CHART_ZOOM_TYPE = pOptions.chart.zoomType || CHART_ZOOM_TYPE;
						CHART_PANNING_ENABLE = pOptions.chart.panning || CHART_PANNING_ENABLE;
						CHART_PAN_KEY = pOptions.chart.panKey || CHART_PAN_KEY;	
						if (pOptions.chart.style) {
							CHART_FONT_FAMILY = pOptions.chart.style.fontFamily || CHART_FONT_FAMILY;
						}
						if (pOptions.chart.title) {
							CHART_TITLE_TEXT = pOptions.chart.title.text || CHART_TITLE_TEXT;
						}
						
					}
					if (pOptions.title) {
						TITLE_TEXT = pOptions.title.text || pOptions.title || TITLE_TEXT;
					}
					if (pOptions.xAxis) {
						if (pOptions.xAxis.title) {
							XAXIS_TITLE_TEXT = pOptions.xAxis.title.text || XAXIS_TITLE_TEXT;
							if (pOptions.xAxis.title.style) {
								XAXIS_TITLE_FONT_WEIGHT = pOptions.xAxis.title.style.fontWeight || XAXIS_TITLE_FONT_WEIGHT;
							}
						}
						
					}
					if (pOptions.yAxis) {
						if (pOptions.yAxis.title) {
							YAXIS_TITLE_TEXT = pOptions.yAxis.title.text || YAXIS_TITLE_TEXT;
							if (pOptions.yAxis.title.style) {
								YAXIS_TITLE_FONT_WEIGHT = pOptions.yAxis.title.style.fontWeight || YAXIS_TITLE_FONT_WEIGHT;
							}
						}
						
						YAXIS_TICK_AMOUNT = pOptions.yAxis.tickAmount || YAXIS_TICK_AMOUNT;
						YAXIS_MIN = pOptions.yAxis.min || YAXIS_MIN;
						YAXIS_MAX = pOptions.yAxis.max || YAXIS_MAX;
					}
					if (pOptions.legend) {
						LEGEND_ENABLED = pOptions.legend.enabled || LEGEND_ENABLED;
						LEGEND_ID = pOptions.legend.id || LEGEND_ID;
						USE_LEGEND_STYLESHEET = pOptions.legend.useStylesheet || USE_LEGEND_STYLESHEET;
						LEGEND_ALIGN = pOptions.legend.align || LEGEND_ALIGN;
						LEGEND_VERTICAL_ALIGN = pOptions.legend.verticalAlign || LEGEND_VERTICAL_ALIGN;
						LEGEND_LAYOUT = pOptions.legend.layout || LEGEND_LAYOUT;
					}
					
					if(pOptions.tooltip) {
						TOOLTIP_FOLLOW_POINTER = pOptions.tooltip.followPointer || TOOLTIP_FOLLOW_POINTER;
						TOOLTIP_BG_COLOR = pOptions.tooltip.backgroundColor || TOOLTIP_BG_COLOR;
						TOOLTIP_BORDER_COLOR = pOptions.tooltip.borderColor || TOOLTIP_BORDER_COLOR;
					}
					
				}
				
				lOptions = {
					chart : {
						renderTo: pDivID,
						type : CHART_TYPE,
						//height: CHART_HEIGHT,
						//width: CHART_WIDTH,
						style: {
				            fontFamily: CHART_FONT_FAMILY
				        },
						zoomType: CHART_ZOOM_TYPE,
						panning: CHART_PANNING_ENABLE,
						panKey: CHART_PAN_KEY,
						events: {
			                load: function () {
			                	if (!LEGEND_ENABLED) {
			                		var chart = this;
			                		var legend = $('#' + LEGEND_ID);
			                		if (legend.length > 0) {
			                			legend.empty();
					                    $(chart.series).each(function (i, serie) {
					                    	if (serie.visible) {
					                    		var lBorderColor = serie.options.borderColor;
					                    		var lColor = serie.options.color;
					                    		$('<li style="color: ' + lBorderColor + '"><span class="bullet" style="width:12px;height:12px;border:1px solid ' + lBorderColor + ';background-color:' + lColor + '"></span><span>' + serie.name + '</span></li>')
					                    		.click(function () {
					                    			var lIsVisible = serie.visible;
					                    			lIsVisible ? serie.hide() : serie.show();
					                    			
						                            $(this).css({
						                            	color: lIsVisible ? 'gray' : lBorderColor
						                            });
						                            
						                            $(this).find('span.bullet').css({
						                            	backgroundColor: lIsVisible ? 'gray' : lColor
						                            });
						                        }).appendTo(legend);
					                    		
					                    		if (!USE_LEGEND_STYLESHEET) {
					                    			legend.css({
					                    				'padding': '0px'
					                    			});
					                    			
					                    			legend.find('li').css({
						                    			'list-style': 'none',
						                    			'float': LEGEND_LAYOUT == 'vertical' ? 'none' : 'left',
						                    			'font-size': '14px',
						                    			'margin-right': '10px'
						                    		}).hover(function() {
						                    			$(this).css({
							                    			'cursor': 'pointer'
							                    		});
						                    		});
						                    		
						                    		legend.find('li span').css({
						                    			'margin-right': '5px',
						                    			'display': 'inline-block'
						                    		});
					                    		}
					                    		
					                    	}
					                    });
			                		}
			                		
			                	} 
			                }
			            }
					},
					title : {
						text : TITLE_TEXT
					},
					xAxis : {
						categories : lChartData.categories,
						type: 'datetime',
						title: {
							text: XAXIS_TITLE_TEXT,
							style: {
								fontWeight: XAXIS_TITLE_FONT_WEIGHT
							}
						}
					},
					yAxis: {
						title: {
							text: YAXIS_TITLE_TEXT,
							style: {
								fontWeight: YAXIS_TITLE_FONT_WEIGHT
							}
						},
						tickAmount: YAXIS_TICK_AMOUNT,
						min: YAXIS_MIN,
						max: YAXIS_MAX
					},
					legend: {
						enabled: LEGEND_ENABLED,
						align: LEGEND_ALIGN,
						verticalAlign: LEGEND_VERTICAL_ALIGN,
						layout: LEGEND_LAYOUT
					},
					tooltip: {
						formatter: function() {
							return cReports.charts.mFormatSeriesChartTooltip(this);
						},
						followPointer: TOOLTIP_FOLLOW_POINTER,
						style: {
							zIndex: 2
						},
						useHTML: true,
						backgroundColor: TOOLTIP_BG_COLOR,
						borderColor: TOOLTIP_BORDER_COLOR
					},
					credits : {
						enabled : false
					},
					plotOptions : {
						series: {
			                maxPointWidth: 20,
			                point: {
								events: {
									click: function(e) {
										cReports.charts.mDatapointSelectListener(pDivID, this);
									}
								}
			                }
			            }
					},
					drilldown: {
			            series: []
			        },
					series: lChartData.series
				};
				
				//if (CHART_HEIGHT) {
					lOptions.chart.height = CHART_HEIGHT;
				//}
				//if (CHART_WIDTH) {
					lOptions.chart.width = CHART_WIDTH;
				//}
				
				
				lOptions.plotOptions[CHART_TYPE] = {
					stacking: 'normal'
				};
				
				var lChart = new Highcharts.Chart(lOptions);
				this.chartObj = lChart;
				return lChart;
			},
			
			/**
			 * Converts data fetched from API response to data format required by 'Timeline' type of Highcharts
			 */
			mBuildTimelineChartData : function(pAttendanceList) {
				var lEmployees = cReports.users.USERS.reverse();
				var lEmployeesAttendance = pAttendanceList.subordinatesMainSwipes;
				var lSelectedDate = pAttendanceList.selectedDate;
				var lChartData = {};
				var lEmployeeNames = [];
				var lElements = [];
				var lSeriesData = [];
				
/*				lEmployees.push({
					"userId": "0",
				    "name": "Emp Name",
				    "holidayId": 1,
				    "branch": ""
				});*/
				$.each(lEmployees,
								function(lEmpCount, lEmployee) {
								    
									var lAttendanceDetails = lEmployeesAttendance[lEmployee.userId];
									var lTotalEffectiveHours = 0;
									if (lAttendanceDetails && lAttendanceDetails.length > 0) {
										
										$.each(lAttendanceDetails,
												function(index, lAttendance) {
											var lDataObj = {};
											var lEffectiveHours = lAttendance.swipeEffectiveHours 
																	/ cReports.util.HOURS_SCALE;
											if (lAttendance.type === 'leaves') {
												lEffectiveHours = 0;
											}
											//lTotalEffectiveHours = lTotalEffectiveHours + lEffectiveHours;

											var lShiftStartTime = new Date(lAttendance.inTime);
											var lShiftEndTime = new Date(lAttendance.outTime);
			
											var lLegends = cReports.util.LEGEND_DETAILS;
											var lColor = lLegends['present'].color;

											lColor = lLegends[lAttendance.type].color;
												
											
											lDataObj = {
												x: 	lShiftStartTime.getTime(),
												x2: lShiftEndTime.getTime(),
												y: lEmpCount,
												date: lAttendance.date,
												text: lEmployee.name,
												label: 'Effective Hours',
												type: lAttendance.type,
												attendanceDetails: lAttendance,
												color: lColor,
												borderColor: lColor,
												effectiveHours: lEffectiveHours
											};
											
											lSeriesData.push(lDataObj);
											
										});
										
										lTotalEffectiveHours = (lAttendanceDetails[lAttendanceDetails.length -1].totalEffectiveHours / cReports.util.HOURS_SCALE);
										lSeriesData[lSeriesData.length - 1].totalEffectiveHours = lAttendanceDetails[lAttendanceDetails.length -1].totalEffectiveHours;
									}
									var lFreq = 'daily';
									var lClickAction =  'cReports.util.mDrillDown('
														+ '\'' + lEmployee.userId + '\', ' 
														+ '\'' + lFreq + '\', ' +
														lSelectedDate	+ ',' + 
														lSelectedDate + ')'; 
									var lStyle = 'cursor:pointer;';
									var lMarginLeft = '15px';
									if (cReports.util.mGetCurrentRequestDetails().userId === lEmployee.userId) {
										lStyle = 'color: orange;';
										lClickAction='';
									}
									
									var lEffectiveFormatted = cReports.util.mFormatNumber(lTotalEffectiveHours);

									lEmployeeNames.push(
										'<table><tr title="'+ lEmployee.name +'" style="' + lStyle + '" onclick="' + lClickAction + '"><td>' + lEmployee.name + '</td>' +
										'<td><b style="margin-left:' + lMarginLeft + '">' +  lEffectiveFormatted + '</b></td></tr></table>'
									);
									lElements.push(lEmployee.name);
								});
	
				lChartData.series = lSeriesData;
				lChartData.categories = lEmployeeNames;
				lChartData.elements = lElements;
				lChartData.selectedDate = lSelectedDate;
				return lChartData;
			},
			/**
			 * Uses the processed chart data(having every swipe details) and returns data in same format but with reduced swipe details.
			 * Te data is returned as 2 pairs of swipe details hence is used to render 2 segments of Timeline Graph - (Swipe Effective Hours and Break Time)
			 * @param 		pChartData  Processed Chart Data having every swipe details of particular user.
			 * @returns		ChartData	Packed Chart Data will be returned
			 */
			mPackTimelineChartData: function(pChartData) {
				var lPackedChartData = [];
				var lElements = pChartData.elements;
				var lSeriesData = pChartData.series;
				var lAttendanceDetails = lSeriesData;
				var lDate = pChartData.selectedDate;
				var lCurElement = '';
				var lElmIndex = 0;
				var lElmTotalEff = {};
				var lOfficeDetails = {};
				$.each(lElements, function(lElCount, lElement) {
					var lEmployee = cReports.users.mGetUserByName(lElement);
					if (lEmployee.baseUser) {
						console.log(JSON.stringify(lEmployee));
						var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
						lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
						console.log(lOfficeDetails.shiftStart);
					}
					
				});
				$.each(lElements, function(lElCount, lElement) {

					var lSCount = 0;
					var lSwipeElapsedTime = 0;
					var lTotalEffectiveHours = 0;
					var lBreakTime = 0;
					var lSwipeInTime = lDate;
					var lSwipeOutTime = lDate;
					var lIsOnLeave = false;
					
					
					$.each(lSeriesData, function(index, lSeriesObj) {
						if (lSeriesObj.text === lElement) {
							
							
							/*if (lElement !== lCurElement) {
								lElmIndex = index;
								lCurElement = lElement;
								lTotalEffectiveHours = 0;
								
							}*/
							
							if (lSCount === 0) {
								lSwipeInTime = lSeriesObj.x;
							}
							if (lSwipeInTime > lSeriesObj.x) {
								lSwipeInTime = lSeriesObj.x;
							}
							if (lSwipeOutTime < lSeriesObj.x2) {
								lSwipeOutTime = lSeriesObj.x2;
							}
							
							if (lSeriesObj.type === 'present' || lSeriesObj.type === 'officialOut') {
								//lTotalEffectiveHours = lTotalEffectiveHours + lSeriesObj.effectiveHours;
							}
							lDate = lSeriesObj.date;
							
							if (lSeriesObj.type === 'leaves') {
								lIsOnLeave = true;
							}
							lSCount++;
							lTotalEffectiveHours = lTotalEffectiveHours + (lAttendanceDetails[index].totalEffectiveHours||0);

							lElmTotalEff[lElement] = lTotalEffectiveHours;
							
						}
						
					});
					//lTotalEffectiveHours = lTotalEffectiveHours  / cReports.util.HOURS_SCALE;
					
	
					
					
					lSwipeElapsedTime = (lSwipeOutTime - lSwipeInTime)/ cReports.util.HOURS_SCALE;
					lBreakTime = Math.abs(lSwipeElapsedTime - (lElmTotalEff[lElement]/cReports.util.HOURS_SCALE));
	
					// For Total Effective Hours
					var lShiftStartTime = lOfficeDetails.shiftStart;
					var lSwipeIn = new Date(lSwipeInTime).setHours(lShiftStartTime.hours, lShiftStartTime.minutes, 0, 0);
					var lSwipeOut = new Date(lSwipeIn).addMilliseconds(lElmTotalEff[lElement]).getTime();
	
					var lDataObj = {
							x: 	lSwipeIn,
							x2: lSwipeOut,
							date: lDate,
							y: lElCount,
							text: lElement,
							label: 'Effective Hours',
							//type: lIsOnLeave == true ? 'leaves' : 'effectiveHours',
							type: 'effectiveHours',
							effectiveHours: lElmTotalEff[lElement]/cReports.util.HOURS_SCALE
						};
					lPackedChartData.push(lDataObj);
					
					// For Break time
					var lSwipeIn = lSwipeOut;
					var lSwipeOut = new Date(lSwipeIn).addMilliseconds(lBreakTime * cReports.util.HOURS_SCALE).getTime();
	
					var lDataObj = {
							x: 	lSwipeIn,
							x2: lSwipeOut,
							date: lDate,
							y: lElCount,
							text: lElement,
							label: 'Break Time',
							type: lIsOnLeave == true ? 'leaves' : 'breakTime',
							//type: 'breakTime',
							color: '#FFFFFF',
							effectiveHours: lBreakTime
						};
					lPackedChartData.push(lDataObj);
					
				});
				pChartData.series = lPackedChartData;
				return pChartData;
			},
			mFormatTimelineChartTooltip: function(pChartObj, pIsPacked) {
				var lName = pChartObj.point.text;
				var lShiftStartTime = new Date(pChartObj.point.x);
				var lShiftEndTime = new Date(pChartObj.point.x2);
				var lEffHours = pChartObj.point.effectiveHours;
				var lLeaveDetails = pChartObj.point.attendanceDetails ? pChartObj.point.attendanceDetails.leaveDetails : [];
				var lRemarks = (pChartObj.point.attendanceDetails && pChartObj.point.attendanceDetails.remarks) ? pChartObj.point.attendanceDetails.remarks : '';
				var lType = pChartObj.point.type;
				var lLabel = pChartObj.point.label;
				
				var lHtmlContent = '';
				lHtmlContent =  '<table>';
				lHtmlContent +=   '<tr>' +
								  	'<td colspan=2><b>' + lName + '</b></td>' +  // Name is mandatory in tooltip
								  '</tr>';
				
				// If user is not on Leave (can be Present, On Tour, On Duty)
				if (lType !== 'leaves') {
					
					// If chart is not packed type and User is present, then display In Time and Out Time
					if (!pIsPacked && lType === 'present') {
						lHtmlContent +=  '<tr>'+
											'<td>In Time &nbsp</td><td><b>' + lShiftStartTime.toString('HH: mm') + '</b></td>'+
										 '</tr>' +
										 '<tr>'+
										 	'<td>Out Time &nbsp</td><td><b>' + lShiftEndTime.toString('HH: mm') + '</b></td>'+
										 '</tr>';
					}

					// Based on Label set while generating chart data, 
					// 'Effective Hours' or 'Break Time' will be displayed along with the value
					lHtmlContent +=   '<tr>' +
										'<td>' + lLabel + ' &nbsp</td><td><b>' + cReports.util.mFormatNumber(lEffHours, true) + '</b></td>'+
									  '</tr>';
				
					// when 'present' 	: ''
					// when 'tour' 		: 'On Tour'
					// when 'onDuty' 	: 'On Duty' 
					lHtmlContent +=  '<tr>'+
										'<td><b>' + cReports.util.LEGEND_DETAILS[lType].text + '</b></td>'+
									 '</tr>';
					
					lHtmlContent +=  '<tr>'+
										'<td>' + lRemarks + '</td>'+
									 '</tr>';
				} else {
					// Leave description is available only if chart is not packed type.
					// When leave details available and list is > 0, then display leave details in table format
					if (lLeaveDetails && lLeaveDetails.length > 0) {
						lHtmlContent +=  this.mGetLeaveDetailsHTML(lLeaveDetails);
					} else {
						// Display as 'On Leave' when leave details are not available
						lHtmlContent +=  '<tr>'+
											'<td><b>' + cReports.util.LEGEND_DETAILS[lType].text + ' &nbsp</b></td>'+
										 '</tr>';
					}
				}

				 
				lHtmlContent += '</table>';
	
				return lHtmlContent;
			},
			
			mSetCategoriesTitle: function(pDivID, pCategories, pTitles) {

				var lTitleDiv = '';
				lTitleDiv += '<div class="categories-title" style="font-size: 12px; font-weight: bold">'; 
				if (pTitles instanceof Array) {
					$.each(pTitles, function(i, lTitle) {
						var lStyle = 'display: inline-block;width: 100px;margin: 0px 0px 0px 15px;';
						var lOffsetWidth = cReports.charts.chartObj.chartWidth - cReports.charts.chartObj.plotWidth;
						if (i === 0) {
							lStyle += 'text-align: right;';
							lStyle += 'width:'+ (lOffsetWidth - 80) + 'px;';
						}
						lTitleDiv += '<div style="' + lStyle + '">  ' + lTitle + '</div>';
					});
				} else {
					lTitleDiv += '<div>' + pTitles + '</div>';
				}
				lTitleDiv += '</div>';
				$('#' + pDivID).prepend(lTitleDiv);

			},
			/**
			 * Renders the timeline chart based on options and series data.
			 */
			mDrawTimelineChart: function(pDivID, 
										 pOptions,
										 pAttendanceList, 
										 pChartType, 
										 pIsPacked, 
										 pFrequency) {
	
				var lSelectedDate = new Date(pAttendanceList.selectedDate);
				cReports.util.minExtreme = lSelectedDate.setHours(0, 0, 0, 0);
				cReports.util.maxExtreme = lSelectedDate.setHours(23, 59, 59, 999);
				
				var lChartData = this.mBuildChartData(pAttendanceList, pChartType);
				if (pIsPacked) {
					lChartData = this.mPackTimelineChartData(lChartData);
				}
				
				var lEmployee = cReports.users.mGetUserById(cReports.util.mGetCurrentRequestDetails().userId);
				var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
				var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
				
				var lShiftStartTime = lOfficeDetails.shiftStart;
				var lShiftEndTime = lOfficeDetails.shiftEnd;
				var lLuchBegin = lOfficeDetails.breakStart;
				var lLuchEnd = lOfficeDetails.breakEnd;
	
				var CHART_TYPE = pChartType;
				var CHART_WIDTH;  // Not initialized to avoid default value setting for chart
				var CHART_HEIGHT; // Not initialized to avoid default value setting for chart
				var CATEGORIES_TITLE_ENABLE = true;
				var CATEGORIES_TITLE_TEXTS = ['Employee Name', 'Eff Hrs'];
				var CHART_FONT_FAMILY = 'arial';
				//var CHART_TYPE = 'bar';
				var CHART_ZOOM_TYPE = 'xy';
				var CHART_PANNING_ENABLE = true;
				var CHART_PAN_KEY = 'shift';
				var TITLE_TEXT = '';
				var XAXIS_TYPE = 'datetime';
				var XAXIS_TITLE_TEXT = 'Time';
				var XAXIS_TITLE_FONT_WEIGHT = 'bold';
				var XAXIS_TICK_INTERVAL = 1000 * 60 * 60;
				var XAXIS_TIMELABEL_FORMAT = '%H:%M';
				var XAXIS_MIN = lSelectedDate.setHours(0, 0, 0, 0);
				var XAXIS_MAX = lSelectedDate.setHours(23, 59, 59, 0);
				var XAXIS_PLOT_LINES = [{
	                value: lSelectedDate.setHours(lShiftStartTime.hours, lShiftStartTime.minutes, 0, 0),
	                color: '#FFC00E',
	                dashStyle: 'shortdash',
	                width: 2,
	                zIndex: 5,
	                label: {
	                	rotation: 0,
	                    y: 0,
	                    x: -17,
	                    text: lShiftStartTime.hours + ':' + lShiftStartTime.minutes
	                }
	            },{
	                value: lSelectedDate.setHours(lLuchBegin.hours, lLuchBegin.minutes, 0, 0),
	                color: '#FFC00E',
	                dashStyle: 'shortdash',
	                width: 2,
	                zIndex: 5,
	                label: {
	                	rotation: 0,
	                	y: 0,
	                    x: -25,
	                    text: lLuchBegin.hours + ':' + lLuchBegin.minutes
	                }
	            },{
	                value: lSelectedDate.setHours(lLuchEnd.hours, lLuchEnd.minutes, 0, 0),
	                color: '#FFC00E',
	                dashStyle: 'shortdash',
	                width: 2,
	                zIndex: 5,
	                label: {
	                	rotation: 0,
	                	y: 0,
	                    x: 0,
	                    text: lLuchEnd.hours + ':' + lLuchEnd.minutes
	                }
	            },{
	                value: lSelectedDate.setHours(lShiftEndTime.hours, lShiftEndTime.minutes, 0, 0),
	                color: '#FFC00E',
	                dashStyle: 'shortdash',
	                width: 2,
	                zIndex: 5,
	                label: {
	                	rotation: 0,
	                	y: 0,
	                    x: 0,
	                    text: lShiftEndTime.hours + ':' + lShiftEndTime.minutes
	                }
	            }];
				var YAXIS_TITLE_TEXT = 'Employees';
				var YAXIS_TITLE_FONT_WEIGHT = 'bold';
				var YAXIS_LABEL_HTML = true;
				var YAXIS_LABEL_PADDING = 10;
				var YAXIS_LABEL_DISTANCE = 30;
				var LEGEND_ENABLED = false;
				var SCROLLBAR_ENABLED = true;
				var SERIES_BORDER_RADIUS = 0;
				var SERIES_BORDER_COLOR = '#7cb5ec';
				var SERIES_BORDER_POINTWIDTH = undefined;
				
				if (pOptions && pOptions instanceof Object) {
					if (pOptions.chart) {
						CHART_HEIGHT = (pOptions.chart.height + 70) || CHART_HEIGHT;
						CHART_ZOOM_TYPE = pOptions.chart.zoomType || CHART_ZOOM_TYPE;
						CHART_PANNING_ENABLE = pOptions.chart.panning || CHART_PANNING_ENABLE;
						CHART_PAN_KEY = pOptions.chart.panKey || CHART_PAN_KEY;				
						if (pOptions.chart.style) {
							CHART_FONT_FAMILY = pOptions.chart.style.fontFamily || CHART_FONT_FAMILY;
						}
						CHART_WIDTH = pOptions.chart.width || CHART_WIDTH;
						CHART_HEIGHT = pOptions.chart.height || CHART_HEIGHT;
						if (pOptions.chart.width === 'auto') {
							var lWindowWidth = window.outerWidth - (0.072 * window.outerWidth);
							CHART_WIDTH = lWindowWidth || CHART_WIDTH;
						}
						if (pOptions.chart.height === 'auto') {
							var lWindowHeight = window.outerHeight;
							CHART_HEIGHT = (lWindowHeight + 70) || CHART_HEIGHT;
						}
						
					}
					// User defined
					if (pOptions.categoriesTitle) { 
						CATEGORIES_TITLE_ENABLE = pOptions.categoriesTitle.enable || CATEGORIES_TITLE_ENABLE;
						CATEGORIES_TITLE_TEXTS = pOptions.categoriesTitle.texts || CATEGORIES_TITLE_TEXTS;
					}
					if (pOptions.title) {
						TITLE_TEXT = pOptions.title.text || pOptions.title || TITLE_TEXT;
					}
					if (pOptions.xAxis) {
						XAXIS_TYPE = pOptions.xAxis.type || XAXIS_TYPE;
						XAXIS_TICK_INTERVAL = pOptions.xAxis.tickInterval || XAXIS_TICK_INTERVAL;
						XAXIS_MIN = pOptions.xAxis.min || XAXIS_MIN;
						XAXIS_MAX = pOptions.xAxis.max || XAXIS_MAX;
						XAXIS_PLOT_LINES = pOptions.xAxis.plotLines || XAXIS_PLOT_LINES;
						if (pOptions.xAxis.title) {
							XAXIS_TITLE_TEXT = pOptions.xAxis.title.text || XAXIS_TITLE_TEXT;
							if (pOptions.xAxis.title.style) {
								XAXIS_TITLE_FONT_WEIGHT = pOptions.xAxis.title.style.fontWeight || XAXIS_TITLE_FONT_WEIGHT;
							}
						}
						if (pOptions.xAxis.dateTimeLabelFormats) {
							XAXIS_TIMELABEL_FORMAT = pOptions.xAxis.dateTimeLabelFormats.day || XAXIS_TIMELABEL_FORMAT;
						}	
					}
					if (pOptions.yAxis) {
						if (pOptions.yAxis.labels) {
							YAXIS_LABEL_HTML = YAXIS_LABEL_HTML || pOptions.yAxis.labels.useHTML;
							YAXIS_LABEL_PADDING = pOptions.yAxis.labels.padding || YAXIS_LABEL_PADDING;
							YAXIS_LABEL_DISTANCE = pOptions.yAxis.labels.distance || YAXIS_LABEL_DISTANCE;
						}
						if (pOptions.yAxis.title) {
							YAXIS_TITLE_TEXT = pOptions.yAxis.title.text || YAXIS_TITLE_TEXT;
							if (pOptions.yAxis.title.style) {
								YAXIS_TITLE_FONT_WEIGHT = pOptions.yAxis.title.style.fontWeight || YAXIS_TITLE_FONT_WEIGHT;
							}
						}
					}
					if (pOptions.legend) {
						LEGEND_ENABLED = pOptions.legend.enabled || LEGEND_ENABLED;
					}
					if (pOptions.scrollbar) {
						SCROLLBAR_ENABLED = pOptions.scrollbar.enabled || SCROLLBAR_ENABLED;
					}
					if (pOptions.series) {
						SERIES_BORDER_RADIUS = pOptions.series.borderRadius || SERIES_BORDER_RADIUS;
						SERIES_BORDER_COLOR = pOptions.series.borderColor || SERIES_BORDER_COLOR;
						SERIES_BORDER_POINTWIDTH = pOptions.series.pointWidth || SERIES_BORDER_POINTWIDTH;
					}
				}
				
				
				////////////////////// Highcharts Options settings /////////////////////
				lOptions = {
					chart : {
						renderTo: pDivID,
						//height: CHART_HEIGHT,
						//width: CHART_WIDTH,
						type : CHART_TYPE,
						zoomType: CHART_ZOOM_TYPE,
						panning: CHART_PANNING_ENABLE,
						panKey: CHART_PAN_KEY,
						style: {
				            fontFamily: CHART_FONT_FAMILY
				        },
				        events: {
				        	click: function(event) {
			                	if (event.target.textContent === 'Reset zoom') {
			                		lChart.xAxis[0].setExtremes(cReports.util.minExtreme, cReports.util.maxExtreme);
			                	}
			                	
			                }
				        }
		                
					},
					title : {
						text : TITLE_TEXT
					},
					xAxis : {
						type: XAXIS_TYPE,	
						tickInterval: XAXIS_TICK_INTERVAL,
						min: XAXIS_MIN,
						max: XAXIS_MAX,
						plotLines: XAXIS_PLOT_LINES,
						title: {
							text: XAXIS_TITLE_TEXT,
							style: {
								fontWeight: XAXIS_TITLE_FONT_WEIGHT
							}
						},
						dateTimeLabelFormats: {
			                day: XAXIS_TIMELABEL_FORMAT
			            }
					},
					yAxis: {
						categories : lChartData.categories,
						min: 0,
				        max: lChartData.categories.length - 1,
				        labels: {
							 useHTML: YAXIS_LABEL_HTML,
							 padding: YAXIS_LABEL_PADDING,
							 distance: YAXIS_LABEL_DISTANCE,
							 zIndex:0
						},
						title: {
							text: YAXIS_TITLE_TEXT,
							style: {
								fontWeight: YAXIS_TITLE_FONT_WEIGHT
							}
						}
					},
					credits : {
						enabled : false
					},
					tooltip: {
						formatter: function() {
							return cReports.charts.mFormatTimelineChartTooltip(this, pIsPacked);
						},
						useHTML: true,
						followPointer: true,
						backgroundColor: 'rgba(255, 255, 255, 1)',
						borderColor: '#9E9E9E' // light gray
					},
					legend: {
						enabled: LEGEND_ENABLED
					},
					scrollbar: {
				        enabled: SCROLLBAR_ENABLED
				    },
					series: [{
						borderRadius: SERIES_BORDER_RADIUS,
						borderColor: SERIES_BORDER_COLOR,
			            pointWidth: SERIES_BORDER_POINTWIDTH,
		                /*dataLabels: {
		                	enabled: true,
		                	align: 'left',
		                	inside: true,
		                	crop: false,
	                		overflow: 'none',
		                	style: {
		                		fontSize: '9px'
		                	},
		                	formatter: function() {
		                		return cReports.util.mFormatNumber(this.point.effectiveHours);
		                	}
		                },*/
						data: lChartData.series,
			            point: {
							events: {
								click: function(e) {
									cReports.charts.mDatapointSelectListener(pDivID, this);
								}
							}
		                }
					}]
				};
				
				//if (CHART_HEIGHT) {
					lOptions.chart.height = CHART_HEIGHT;
				//}
				//if (CHART_WIDTH) {
					lOptions.chart.width = CHART_WIDTH;
				//}
				
				var lChart = new Highcharts.Chart(lOptions);
				
				var lMinExtreme = lSelectedDate.setHours(8, 0, 0, 0);
				var lMaxExtreme = lSelectedDate.setHours(22, 0, 0, 0);
				
				
				if(lChartData.series.length > 0) {
					if (!(lChartData.series[0].x < lMinExtreme || lChartData.series[lChartData.series.length - 1].x2 > lMaxExtreme )) {
						lChart.xAxis[0].setExtremes(lMinExtreme, lMaxExtreme);
						cReports.util.minExtreme = lMinExtreme;
						cReports.util.maxExtreme = lMaxExtreme;
						
					}
				}
				 
				this.chartObj = lChart;
				
				if (CATEGORIES_TITLE_ENABLE) {
					this.mSetCategoriesTitle(pDivID, lChartData.categories, ['Emp Name', 'Eff Hrs']);
				}
				
				return lChart;
			}
		},
	
		/**
		 * APIs for grid component
		 */
		grid : {
			mBuildDailyData: function(pResult, pFrequency) {
				var lUsersAttendance = pResult.subordinatesAttendance;
				var lSelectedDate = new Date(pResult.selectedDate);
				var lUsersList = cReports.users.USERS;
				var lHeaderData = [];
				var lUserAttendanceData = [];
				var lAttendanceData = {};
				
				var lMinReqHoursHalfDay = 0;
				var lMinReqHoursFullDay = 0;

				lHeaderData = [
				   {/*columnWidth: '220px',*/ text: 'Employee Name', sort: true},
	               //{text: 'On Duty', sort: true},
				   {text: 'On Duty/ Tour', sort: true},
	               {text: 'Leaves Taken', sort: true},
	               {text: 'Reqd Hrs', sort: true},
	               {text: 'Effective Hrs', sort: true},
	               {text: 'Difference', sort: true}
	               
	           ];
					
				
				
				if (lSelectedDate.getTime() === Date.today().getTime()) {
					lHeaderData.push({text: 'First In'});
					lHeaderData.push({columnWidth: '90px', text: 'Location'});
					lHeaderData.push({columnWidth: '230px', text: 'Current Zone'});
				}
					
					var lActualTotalDays = 0;
					var lActualDaysPresent = 0;
					var lActualVacationDays = 0;
					var lReqdEffectiveHours = 0;
					var lActualEffectiveHours = 0;
					
					var totalExcessEffective = 0;
					var totalShortageEffective = 0;
					var totalReqdExcessEffective = 0;
					var totalReqdShortageEffective = 0;
					var lCount = 0;
	
				for (var j = 0; j < lUsersList.length; j++) {
					var lAttendance = lUsersAttendance[lUsersList[j].userId];
					
					var lEmployee = cReports.users.mGetUserById(lUsersList[j].userId);
					var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
					var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
					
					lMinReqHoursHalfDay = lOfficeDetails.requiredHoursForHalfDay.toHours();
					lMinReqHoursFullDay = lOfficeDetails.requiredHoursForFullDay.toHours();
					
					if (lAttendance) {
						lActualTotalDays = lActualTotalDays + lAttendance.workingDays;
						//var lOnDutyHours = lAttendance.onDuty.toFixed(2) * cReports.util.WORKING_HOURS;
						var lTourHours = lAttendance.tour.toFixed(2) * lOfficeDetails.workingHours;
						lActualDaysPresent = lActualDaysPresent + lAttendance.daysPresent;
						lActualVacationDays = lActualVacationDays + lAttendance.leavesTaken;
						
						var lWorkingDaysFraction = lAttendance.workingDays/1;
						var lWorkingDaysInteger = lAttendance.workingDays - lWorkingDaysFraction;
						
						lReqdEffectiveHours = lReqdEffectiveHours + (lWorkingDaysFraction) * (lMinReqHoursHalfDay * 2);
						lReqdEffectiveHours = lReqdEffectiveHours + (lWorkingDaysInteger) * lMinReqHoursFullDay;
						//lReqdEffectiveHours = lReqdEffectiveHours + (lAttendance.workingDays) * cReports.util.FULL_DAY_REQ_HOURS;
						//lActualEffectiveHours = +lActualEffectiveHours + +lAttendance.effectiveHours + +lOnDutyHours;
						
						lUserAttendanceData[lCount] = [];
						lUserAttendanceData[lCount].push({text: lUsersList[j].name});
						//lUserAttendanceData[lCount].push({text: lAttendance.onDuty.toFixed(2)});
						lUserAttendanceData[lCount].push({text: lAttendance.tour.toFixed(2)});
						lUserAttendanceData[lCount].push({text: lAttendance.leavesTaken.toFixed(2)});
						
						var lRequiredHours = cReports.util.mFormatNumber(lAttendance.requiredHours, true);
						lUserAttendanceData[lCount].push({text: lRequiredHours});
						
						//var lEffectiveHours = cReports.util.mFormatNumber((lAttendance.effectiveHours + +lOnDutyHours), true);
						var lEffectiveHours = cReports.util.mFormatNumber((lAttendance.effectiveHours + +lTourHours), true);
						lUserAttendanceData[lCount].push({text: lEffectiveHours});
						
						
						var lColor = '';
						//var lDifference = lAttendance.difference + lOnDutyHours;
						var lDifference = lAttendance.difference + lTourHours;
						var lDifferenceFmtd = cReports.util.mFormatNumber(lDifference, true);
	
						if (lDifference > 0) {
							lColor = cReports.util.LEGEND_DETAILS['excess'].color;
							//totalExcessEffective = +totalExcessEffective + +lAttendance.effectiveHours + +lOnDutyHours;
							totalExcessEffective = +totalExcessEffective + +lAttendance.effectiveHours + +lTourHours;
							totalReqdExcessEffective = +totalReqdExcessEffective + +lAttendance.requiredHours;
						} else {
							lColor = cReports.util.LEGEND_DETAILS['shortage'].color;
							//totalShortageEffective = +totalShortageEffective + +lAttendance.effectiveHours + +lOnDutyHours;
							totalShortageEffective = +totalShortageEffective + +lAttendance.effectiveHours + +lTourHours;
							totalReqdShortageEffective = +totalReqdShortageEffective + +lAttendance.requiredHours;
						}
						
						lUserAttendanceData[lCount].push({text: lDifferenceFmtd, color: lColor});
						
						if (lSelectedDate.getTime() === Date.today().getTime()) {
							
							var lUserFirstInTime = lAttendance.swipeInTime == undefined ? '' : lAttendance.swipeInTime.getDateTime();
							lUserFirstInTime = lAttendance.daysPresent == 0 ? '-' : lUserFirstInTime.toString('HH:mm');
							lUserAttendanceData[lCount].push({text: lUserFirstInTime});
							
							lUserAttendanceData[lCount].push({text: lUsersList[j].branch});
							
							var lUserLastSwipe = lAttendance.lastSwipe;
							var lExtryExit = '';
							var lDoor = '';
							var lSwipeTime = '';
							if (lUserLastSwipe) {
								lExtryExit = lUserLastSwipe.doorEntryExit;
								lDoor = lUserLastSwipe.doorName;
							    lSwipeTime = new Date(lUserLastSwipe.swipeTime);
							}
							
							
							var lZoneArea = cReports.util.DEFAULT_ZONE;
							$.each(cReports.util.ZONE, function(index, lZone) {
								if(lZone.entryExit == lExtryExit.toLowerCase() && lZone.area == lDoor.toLowerCase()) {
									lZoneArea = lZone.zone;
								}
							});
							
							var lInOut = '';
							
							if (lAttendance.inOffice == true) {
								lInOut = 'IN';
								lSwipeTime = '';
							} else {
								lInOut = 'OUT';
								if (lSwipeTime.setHours(0,0,0,0) === Date.today().getTime()) {
									lSwipeTime = new Date(lUserLastSwipe.swipeTime).toString('HH:mm');
								} else {
									lSwipeTime = '';
								}
							}
							lUserAttendanceData[lCount].push({ text: '<ul class="zonal-desc" style="list-style: none"><li style="width:40px;float:left">' + lInOut + '</li><li style="width:140px;float:left">' + lZoneArea + '</li><li style="width:30px;float:left">'+ lSwipeTime + '</li></ul>'});
						}
						lCount++;
					}
				}
				
				var lAdditional = 0.0;
				
				var lWorkingDaysFraction = (lActualVacationDays + lAdditional)/1;
				var lWorkingDaysInteger = (lActualVacationDays + lAdditional) - lWorkingDaysFraction;
				
				lReqdEffectiveHours = lReqdEffectiveHours - (lWorkingDaysFraction) * (lMinReqHoursHalfDay * 2);
				lReqdEffectiveHours = lReqdEffectiveHours - (lWorkingDaysInteger) * lMinReqHoursFullDay;
				
				//lReqdEffectiveHours = lReqdEffectiveHours - (lActualVacationDays + lAdditional) * cReports.util.FULL_DAY_REQ_HOURS;
				var lAvgDivisor = pFrequency === 'daily' ? (lActualTotalDays - lActualVacationDays + lAdditional) : lUsersAttendance.length;
				
				lReqdAvgEffectiveHours = lReqdEffectiveHours/lAvgDivisor;
				lActualAvgEffectiveHours = lActualEffectiveHours/lAvgDivisor;
	
				lAttendanceData.headerData = lHeaderData;
				lAttendanceData.usersAttendanceData = lUserAttendanceData;
				
				return lAttendanceData;
			},
			mGetDateByFrequency: function(pAttendanceData, pFrequency) {
				var lDate = '';
				if (pFrequency === 'monthly') {
					lDate = cReports.util.MONTH_NAMES[pAttendanceData.month - 1] + ', ' + pAttendanceData.year;
				} else if (pFrequency === 'weekly') {
					lDate = cReports.util.WEEK_PREFIX + pAttendanceData.week + cReports.util.WEEK_YEAR_DELIM + pAttendanceData.year;
				} else if (pFrequency === 'daily') {
					lDate = new Date(pAttendanceData.date).toString(cReports.util.DATE_FORMAT);
				}
				return lDate;
			},
			mBuildFrequencyData: function(pResult, pFrequency) {
				var lUsersAttendance = pResult.userAttendance;
				var lUserID = pResult.userId;
				var lEmployee = cReports.users.mGetUserById(lUserID);
				var lRawOfficeDetails = cReports.util.mGetOfficeDetailsByLocation(lEmployee.holidayId, cReports.util.OFFICE_DETAILS );
				var lOfficeDetails = cReports.util.mExtractOfficeDetails(lRawOfficeDetails);
				
				var lFromDate = new Date(pResult.fromDate);
				var lToDate = new Date(pResult.toDate);
				var lHeaderData = [];
				var lUserAttendanceData = new Array(7);
				var lAttendanceData = {};
				
				lHeaderData.push({text: '', columnWidth: '250px', clickAction: '', sort: false});
				//lUsersAttendance.sort(cReports.util.mSortItems(pFrequency, 'grid'));
				for (var i = 0; i < lUsersAttendance.length; i++) {
					var lAttendance = lUsersAttendance[i];
					var lHeaderObj = {};
					lHeaderObj.clickAction = 'cReports.util.mDrillDown(\'' 
											+ cReports.util.FREQUENCY[pFrequency.toLowerCase()].drill + '\', ' +
											lAttendance.beginDate	+ ',' + 
											lAttendance.endDate + ')'; 
					
					lHeaderObj.text = this.mGetDateByFrequency(lAttendance, pFrequency);
					if (pFrequency.toLowerCase() === 'daily') {
						lHeaderObj.toolTip = new Date(lAttendance.date).toString(cReports.util.DATE_FORMAT);
					} else {
						lHeaderObj.toolTip = new Date(lAttendance.beginDate).toString(cReports.util.DATE_FORMAT) 
											 + ' - ' + 
											 new Date(lAttendance.endDate).toString(cReports.util.DATE_FORMAT);
					}
					
					lHeaderObj.sort = false;
					lHeaderObj.columnWidth = '80px';
					lHeaderObj.attendance = lAttendance;
					lHeaderData.push(lHeaderObj);
				}
	
					lUserAttendanceData = [
						                      [{text: 'Total Working Days'}],
						                      //[{text: 'On Duty'}],
						                      [{text: 'On Duty/ Tour'}],
						                      [{text: 'Leaves Taken'}],
						                      [{text: 'Days Present'}],
						                      [{text: 'Reqd Hours'}],
						                      [{text: 'Effective Hours (ACS)'}],
						                      [{text: 'Difference'}]
					                      ];
					
					
					
					var lActualTotalDays = 0;
					var lActualDaysPresent = 0;
					var lActualVacationDays = 0;
					var lReqdEffectiveHours = 0;
					var lActualEffectiveHours = 0;
					var lReqdAvgEffectiveHours = 0;
					var lActualAvgEffectiveHours = 0;
					
					var totalExcessEffective = 0;
					var totalShortageEffective = 0;
					var totalReqdExcessEffective = 0;
					var totalReqdShortageEffective = 0;
	
	
				for (var j = 0; j < lUsersAttendance.length; j++) {
					var lAttendance = lUsersAttendance[j];
					
					lActualTotalDays = lActualTotalDays + lAttendance.workingDays;
					//var lOnDutyHours = lAttendance.onDuty.toFixed(2) * cReports.util.WORKING_HOURS;
					var lTourHours = lAttendance.tour.toFixed(2) * lOfficeDetails.workingHours;
					lActualDaysPresent = lActualDaysPresent + lAttendance.daysPresent;
					lActualVacationDays = lActualVacationDays + lAttendance.leavesTaken;
					lReqdEffectiveHours = lReqdEffectiveHours + (lAttendance.requiredHours);
					//lActualEffectiveHours = +lActualEffectiveHours + +lAttendance.effectiveHours + +lOnDutyHours;
					lActualEffectiveHours = +lActualEffectiveHours + +lAttendance.effectiveHours + +lTourHours;
					
	
					
					lUserAttendanceData[0].push({text: lAttendance.workingDays.toFixed(2)});
					//lUserAttendanceData[1].push({text: lAttendance.onDuty.toFixed(2)});
					lUserAttendanceData[1].push({text: lAttendance.tour.toFixed(2)});
					lUserAttendanceData[2].push({text: lAttendance.leavesTaken.toFixed(2)});
					lUserAttendanceData[3].push({text: lAttendance.daysPresent.toFixed(2)});
					
					var lRequiredHours = cReports.util.mFormatNumber(lAttendance.requiredHours, true);
					lUserAttendanceData[4].push({text: lRequiredHours});
					
					
					//var lEffectiveHours = cReports.util.mFormatNumber((lAttendance.effectiveHours + +lOnDutyHours), true);
					var lEffectiveHours = cReports.util.mFormatNumber((lAttendance.effectiveHours + +lTourHours), true);
					lUserAttendanceData[5].push({text: lEffectiveHours});
					
					
					var lColor = '';
					//var lDifference = lAttendance.difference + lOnDutyHours;
					var lDifference = lAttendance.difference + lTourHours;
					var lDifferenceFmtd = cReports.util.mFormatNumber(lDifference, true);
	
					if (lDifference > 0) {
						lColor = cReports.util.LEGEND_DETAILS['excess'].color;
						//totalExcessEffective = +totalExcessEffective + +lAttendance.effectiveHours + +lOnDutyHours;
						totalExcessEffective = +totalExcessEffective + +lAttendance.effectiveHours + +lTourHours;
						totalReqdExcessEffective = +totalReqdExcessEffective + +lAttendance.requiredHours;
					} else {
						lColor = cReports.util.LEGEND_DETAILS['shortage'].color;
						//totalShortageEffective = +totalShortageEffective + +lAttendance.effectiveHours + +lOnDutyHours;
						totalShortageEffective = +totalShortageEffective + +lAttendance.effectiveHours + +lTourHours;
						totalReqdShortageEffective = +totalReqdShortageEffective + +lAttendance.requiredHours;
					}
					
					lUserAttendanceData[6].push({text: lDifferenceFmtd, color: lColor});
				}
				
				var lAdditional = 0.0;
				var lWorkingDaysFraction = lAdditional/1;
				var lWorkingDaysInteger = lAdditional - lWorkingDaysFraction;
				
				lReqdEffectiveHours = lReqdEffectiveHours - (lWorkingDaysFraction) * (lOfficeDetails.requiredHoursForHalfDay.toHours() * 2);
				lReqdEffectiveHours = lReqdEffectiveHours - (lWorkingDaysInteger) * lOfficeDetails.requiredHoursForFullDay.toHours();
				
				//lReqdEffectiveHours = lReqdEffectiveHours - ( lAdditional) * cReports.util.FULL_DAY_REQ_HOURS;
				var lAvgDivisor = pFrequency === 'daily' ? (lActualTotalDays - lActualVacationDays + lAdditional) : lUsersAttendance.length;
				
				lReqdAvgEffectiveHours = lReqdEffectiveHours/lAvgDivisor;
				lActualAvgEffectiveHours = lActualEffectiveHours/lAvgDivisor;
	
				lAttendanceData.headerData = lHeaderData;
				lAttendanceData.usersAttendanceData = lUserAttendanceData;
				
				lAttendanceData.summaryTableData = {
						userId: lUserID,
						fromDate: lFromDate,
						toDate: lToDate,
						frequency: cReports.util.FREQUENCY[pFrequency.toLowerCase()].axis,
						actualTotalDays: lActualTotalDays,
						actualDaysPresent: lActualDaysPresent,
						actualVacationDays: lActualVacationDays,
						actualEffectiveHours: lActualEffectiveHours,
						reqdEffectiveHours: isNaN(lReqdEffectiveHours) ? 0 : lReqdEffectiveHours,
						reqdAvgEffectiveHours: isNaN(lReqdAvgEffectiveHours) ? 0 : lReqdAvgEffectiveHours,
						actualAvgEffectiveHours: isNaN(lActualAvgEffectiveHours) ? 0 : lActualAvgEffectiveHours,
						totalExcessEffective: isNaN(totalExcessEffective) ? 0 : totalExcessEffective,
						totalShortageEffective: isNaN(totalShortageEffective) ? 0 : totalShortageEffective,
						totalReqdExcessEffective: isNaN(totalReqdExcessEffective) ? 0 : totalReqdExcessEffective,
						totalReqdShortageEffective: isNaN(totalReqdShortageEffective) ? 0 : totalReqdShortageEffective
				};

				
				return lAttendanceData;
			},
			/**
			 * Delegates to Attendance data builder based on Users and frequency
			 * @param pResult(*)		Object		Response that contains attendance details for given frequency
			 * @param pFrequency(*)		string		Period or Frequency. Possible values - 'daily', 'weekly', 'monthly'
			 * @returns					Function	Handler function to build data
			 */
			mBuildAttendanceData: function(pResult, pFrequency) {
				if (!pResult.usersList) {
					return this.mBuildFrequencyData(pResult, pFrequency);
				} else {
					return this.mBuildDailyData(pResult, pFrequency);
				}
			},
			/**
			 * 
			 * @param pDivId(*)			string		HTML div id to which the grid table is rendered
			 * @param pResult(*)		Object		Response that contains attendance details for given frequency
			 * @param pFrequency(*)		string		Period or Frequency. Possible values - 'daily', 'weekly', 'monthly'
			 * @returns					Object		Baked Attendance data(which is used to render grid), 
			 * 									    so that caller can use this if needed		
			 * 
			 * @chg kcr 2016-04-14		Modified padding under styling grid section, which removes unwanted th below the table header
			 */
			mRenderAttendance: function(pDivId, pResult, pFrequency, pOptions) {
				
				var HEIGHT = 200;
				var PAGINATION = true;
				var PAGE_SIZE = -1;
				var SEARCHABLE = true;
				var RESIZABLE = false;
				var REORDERABLE = true;
				var SCROLLABLE = true;
				
				
				var SORTABLE = true;
				var SORT_ORDER = 'asc';
				var COLUMN_WIDTH = 80;
				var TOOLTIP = '';
				var CLICK_ACTION = '';
				var HEADER_TEXT = '';
				
				var USE_STYLESHEET = false;
				
				
				var lAttendanceData = this.mBuildAttendanceData(pResult, pFrequency);
				var lHeaderData = lAttendanceData.headerData;
				var lUsersAttendanceData = lAttendanceData.usersAttendanceData;
				if (pOptions) {
					
					HEIGHT = pOptions.height == undefined ? HEIGHT :  pOptions.height;
					PAGINATION = pOptions.pagination == undefined ? PAGINATION :  pOptions.pagination;
					PAGE_SIZE = pOptions.pageSize == undefined ? PAGE_SIZE :  pOptions.pageSize;
					SEARCHABLE = pOptions.search == undefined ? SEARCHABLE : pOptions.search;
					RESIZABLE = pOptions.columnResize == undefined ? RESIZABLE : pOptions.columnResize;
					REORDERABLE = pOptions.columnReorder == undefined ? REORDERABLE : pOptions.columnReorder;
					SCROLLABLE = pOptions.scrollable == undefined ? SCROLLABLE : pOptions.scrollable;
					
					
					SORTABLE = pOptions.sortable == undefined ? SORTABLE : pOptions.sortable;
					SORT_ORDER = pOptions.sortOrder == undefined ? SORT_ORDER : pOptions.sortOrder;
					COLUMN_WIDTH = pOptions.columnWidth == undefined ? COLUMN_WIDTH : pOptions.columnWidth;
					TOOLTIP = pOptions.tooltip == undefined ? TOOLTIP : pOptions.tooltip;
					CLICK_ACTION = pOptions.clickAction == undefined ? CLICK_ACTION : pOptions.clickAction;
					HEADER_TEXT = pOptions.headerText == undefined ? HEADER_TEXT : pOptions.headerText;
					
					USE_STYLESHEET = pOptions.useStylesheet == undefined ? USE_STYLESHEET : pOptions.useStylesheet;
				}
				
				
				var lAtendance = $('#' + pDivId);
				lAtendance.empty();
				lAtendance.append('<table id="table" class="display"' +
						 			//'data-sort-order="' + SORT_ORDER + '" ' +
						 			'data-striped="true" ' +
						 			//'data-pagination="' + PAGINATION + '" ' +
						 			//'data-page-size="' + PAGE_SIZE + '" ' +
						 			//'data-search="' + SEARCHABLE + '" ' +
						 			'data-trim-on-search="false" ' +
						 			'data-search-time-out="100" ' +
						 			//'data-resizable="' + RESIZABLE + '" ' +
						 			//'data-page-list="[10, 25, 50, 100, ALL]" ' +
						 			//'data-reorderable-columns="' + REORDERABLE + '" ' +
						 			'data-height="' + HEIGHT + '">' +
						 			'<thead></thead><tbody></tbody></table>');
				
				var thead = lAtendance.find('thead');
				thead.append('<tr></tr>');
				var lTableColumnOptions = [];
				for ( var lColumn = 0; lColumn < lHeaderData.length ; lColumn++ ) {
					
					var lSortable = lHeaderData[lColumn].sort || SORTABLE;
					var lColumnWidth = lHeaderData[lColumn].columnWidth || COLUMN_WIDTH;
					var lTooltip = lHeaderData[lColumn].toolTip || TOOLTIP;
					var lClickAction = lHeaderData[lColumn].clickAction || CLICK_ACTION;
					var lHeaderText = lHeaderData[lColumn].text || HEADER_TEXT;
					
					lTableColumnOptions.push({
						"bSortable": lSortable,
						"bFilter": SEARCHABLE
					});
					thead.find('tr')
							.append('<th ' + 
									//'searchable="true" ' + 
									//'data-sortable="' + lSortable + '" ' + 
									'style="cursor:default;min-width:' + lColumnWidth + '" ' + 
									'title ="' + lTooltip + '" ' +
									'onClickCell="' + lClickAction + '">' 
									+ lHeaderText + 
									'</th>');
	
				}
				
				
				var tbody = lAtendance.find('tbody');
				for ( var lRow = 0; lRow < lUsersAttendanceData.length; lRow++ ) {
					var lCurrentRow = $('<tr></tr>').appendTo(tbody);
					
					for ( var lColumn = 0; lColumn < lUsersAttendanceData[lRow].length; lColumn++ ) {
						var lDetails = lUsersAttendanceData[lRow][lColumn];
						var title = '';
						if (lColumn < 8) {
							title = lDetails.text;
						}
						lCurrentRow.append('<td title="' + title + '" style="color:' + lDetails.color + '">'+lDetails.text+'</td>');
					}
				}
				
				
				//$('#' + pDivId + ' table').bootstrapTable();
				
				var lDom = 'lfrtip';
				if (REORDERABLE) {
					lDom = 'R' + lDom;
				}
				
				var lTableOptions = {
					paging: PAGINATION,
					searching: SEARCHABLE,
					ordering: REORDERABLE,
					order: [[0, SORT_ORDER]]
				};
				
				var lOpts = {
						"aoColumns": lTableColumnOptions,
						"bPaginate": PAGINATION,
						"iDisplayLength": PAGE_SIZE,
						"aLengthMenu": [[ -1, 25, 50, 75], ["All",25, 50, 75 ]],
						"sDom": lDom,
						"sPaginationType": "full_numbers",
						"fnInitComplete": function () {
							//this.fnDraw();
						}
				};
				
				if (SCROLLABLE) {
					lOpts["sScrollY"] = HEIGHT + "px";
				}
				if (!lSortable) {
					lOpts.aaSorting = [];
				}
				$('#' + pDivId + ' table').dataTable(lOpts);
				
				
				if (!USE_STYLESHEET) {
					
					$('#' + pDivId + ' table').css({
						'font-size': '12px'
					});
					
					$('#' + pDivId + ' table td').css({
						'text-overflow': 'ellipsis',
						'overflow': 'hidden',
						'white-space': 'nowrap'
					});
					
					$('#' + pDivId + ' table .zonal-desc').css({
						'list-style': 'none',
						'padding': '0px',
						'margin': '0px'
					});
					
					$('#' + pDivId + ' table thead').css({
						'background-color': '#435784',
						'color': '#ffffff'
					}).find('th').css({
						'padding': '0 2px',
				    	'border': '1px solid'
					});
				    
					var lTable = $('#' + pDivId + ' table');
					/*
					lTable.find('tr:odd').css({
						'background-color': '#F3F3F3'
					});
					
					lTable.find('tr:odd .sorting_1').css({
						'background-color': '#F3F3F3',
						'font-weight': 'bold'
					});
					lTable.find('tr:even .sorting_1').css({
						'background-color': '#FFFFFF',
						'font-weight': 'bold'
					});*/
				}
				
				//$('#' + pDivId + ' table').on('click-cell.bs.table', function(e) {
					//console.log('c');
				//});
				return lAttendanceData;
			},
			
			/**
			 * Renders summary table for given range of period per employee.
			 * Params with (*) are mandatory
			 * @param	pDivId(*)						string			Div id to which the summary table to be rendered
			 * @param	pSummaryData(*)					Object			Data required by summary table to be displayed with.
			 * 															Summary data is generated automatically and returned within object of key 'summaryTableData' 
			 * 															on calling 'mRenderAttendance()' method of 'grid' utility
			 * 
			 * 															summaryTableData = {
																				frequency: 					string		Period or Frequency 
																													    possible values - 'daily', 'weekly', 'monthly'
																				actualTotalDays: 			number		
																				actualDaysPresent: 			number
																				actualVacationDays: 		number
																				actualEffectiveHours: 		number
																				reqdEffectiveHours: 		number
																				reqdAvgEffectiveHours: 		number
																				actualAvgEffectiveHours: 	number
																				totalExcessEffective: 		number
																				totalShortageEffective: 	number
																				totalReqdExcessEffective: 	number
																				totalReqdShortageEffective: number
																		}
																		
			 * @param	pOptions						Object			This component can be customized with few customization options.
			 * 			position: 						string 			Css position 							e.g., 'absolute'
			 * 			align:							string			Horizontal alignment of component 		
			 * 															wrt its parent div 
			 * 															possible values - 'left', 'center', 'right'  
			 * 																
			 * 			width: 							string 			Component width in 'px' or '%'  		e.g., '300px'
			 * 			backgroundColor: 				string			Component background color				e.g., '#EBEBEB'
			 * 			headerBackgroundColor: 			string 			Component's heading background-color	e.g., 'pink',
			 * 			margin: 						string 			Component's margin 						e.g., '-270px 0 0 0',
			 * 			right: 							string 			Absolute margin from right end of container. 
			 * 															This field will be active only if 
			 * 															option 'align' is undefined				e.g., '5%' 
			 * 			labels: {					 	Object			Headings for table
							summary: 'Summary',			string
							name: 						string			e.g., 'Name: '
							period: 					string			e.g., 'Period: '
							stdWorkingDays: 			string			e.g., 'Std. Working Days'
							addDays: 					string 			e.g., 'Additional days of work'
							vacation: 					string 			e.g., 'Vacation'
							effHrsReqd: 				string 			e.g., 'Effective Hrs'
							effHrsActual: 				string 			e.g., 'Effective Hrs'
							avgEffHrsReqd: 				string 			e.g., 'Avg Eff Hrs'
							avgEffHrsActual: 			string 			e.g., 'Avg Eff Hrs'		
					   }
			 */
			mRenderSummary: function(pDivId, pSummaryData, pOptions) {
				
				var USE_STYLE_SHEET = false;
				var POSITION = 'relative';
				var ALIGN = 'window-right';
				var WIDTH = '208px';
				var BORDER = '1px solid';
				var BACKGROUND_COLOR = 'white';
				var HEADER_BACKGROUND_COLOR = '#EBEBEB';
				var FONT_SIZE = '11px';
				var MARGIN = '0 0 0 0';
				var RIGHT = '0px';
				var LABELS = {
					summary: 'Summary',
					name: 'Name: ',
					period: 'Period: ',
					stdWorkingDays: 'Std. Working Days',
					addDays: 'Additional days of work',
					vacation: 'Vacation',
					effHrsReqd: 'Effective Hrs (Reqd)',
					effHrsActual: 'Effective Hrs (Actual)',
					avgEffHrsReqd: 'Avg Eff Hrs (Reqd)',
					avgEffHrsActual: 'Avg Eff Hrs (Actual)',
					
				};
				
				
				if (pOptions) {
					USE_STYLE_SHEET = pOptions.useStylesheet || USE_STYLE_SHEET;
					POSITION = pOptions.position || POSITION;
					ALIGN = pOptions.align || ALIGN;
					WIDTH = pOptions.width || WIDTH;
					BORDER = pOptions.border || BORDER;
					BACKGROUND_COLOR = pOptions.backgroundColor || BACKGROUND_COLOR;
					HEADER_BACKGROUND_COLOR = pOptions.headerBackgroundColor || HEADER_BACKGROUND_COLOR;
					FONT_SIZE = pOptions.fontSize || FONT_SIZE;
					MARGIN = pOptions.margin || MARGIN;
					RIGHT = pOptions.right || RIGHT;
					if (pOptions.labels) {
						LABELS.summary = pOptions.labels.summary || LABELS.summary;
						LABELS.name = pOptions.labels.name || LABELS.name;
						LABELS.period = pOptions.labels.period || LABELS.period;
						LABELS.stdWorkingDays = pOptions.labels.stdWorkingDays || LABELS.stdWorkingDays;
						LABELS.addDays = pOptions.labels.addDays || LABELS.addDays;
						LABELS.vacation = pOptions.labels.vacation || LABELS.vacation;
						LABELS.effHrsReqd = pOptions.labels.effHrsReqd || LABELS.effHrsReqd;
						LABELS.effHrsActual = pOptions.labels.effHrsActual || LABELS.effHrsActual;
						LABELS.avgEffHrsReqd = pOptions.labels.avgEffHrsReqd || LABELS.avgEffHrsReqd;
						LABELS.avgEffHrsActual = pOptions.labels.effHrsReqd || LABELS.avgEffHrsActual;
					}
					
					
					switch(ALIGN) {
						case 'right':  
							RIGHT = '0%';
							break;
						case 'center':
							RIGHT = '40%';
							break;
						case 'left':
							RIGHT = '80%';
							break;
						default:
							//RIGHT = RIGHT || (window.outerWidth * 0.07) + 'px';
					}
				}			
				
				$('#' + pDivId).html('<table>' +
										'<tr>' +
											'<th colspan=3 style="text-align:center"><b>' + LABELS.summary + '</b></th>' +
										'</tr>' +
										'<tr>' +
											'<td colspan=3 style="text-align:left"><b>' + LABELS.name + '</b><span id="summary-name"></span></td>' +
										'</tr>' +
										'<tr>' +
											'<td colspan=3 style="text-align:left"><b>' + LABELS.period + '</b><span id="summary-period"></span></td>' +
										'</tr>' +
										'<tr>' +
											'<th>' + LABELS.stdWorkingDays + '</th><td id="actual-total-days"></td>' +
										'</tr>' +
										'<tr>' +
											'<th>' + LABELS.addDays + '</th><td id="actual-days-present">-</td>' +
										'</tr>' +
										'<tr>' +
											'<th>' + LABELS.vacation + '</th><td id="actual-vacation-days"></td>' +
										'</tr>' +
										'<tr>' +
											'<th>' + LABELS.effHrsReqd + '</th><td id="reqd-effective-hours"></td>' +
										'</tr>' +
										'<tr>' +
											'<th>' + LABELS.effHrsActual + '</th><td id="actual-effective-hours"></td>' +
										'</tr>' +
										'<tr>' +
											'<th id="avg-effective-freq-label">' + LABELS.avgEffHrsReqd + '</th><td id="reqd-effective-hours-cmp"></td>' +
										'</tr>' +
										'<tr>' +
											'<th id="avg-effective-freq-label">' + LABELS.avgEffHrsActual + '</th><td id="actual-effective-hours-cmp"></td>' +
										'</tr>' +
									'</table>' +
									'<div class="summary-sparkline">' +
										'<div id="sparkline"></div>' +
									'</div>');
				
				if (!USE_STYLE_SHEET) {
					$('#' + pDivId).css({
						'position': POSITION,
						'width': WIDTH,
						'border': BORDER,
						'background-color': BACKGROUND_COLOR,
						'margin': MARGIN,
						'right': RIGHT
					});
					
					
					
					var lTable = $('#' + pDivId + ' table');
					lTable.css({
						'margin': 'auto',
						'border-collapse': 'collapse',
						'width': WIDTH
					});
					var lTr = lTable.find('tr');
					lTr.find('th').css({
						'background-color': HEADER_BACKGROUND_COLOR,
						'font-size': FONT_SIZE,
						'border': '1px solid #ADADAD',
						'padding': '2px'
					});
					
					var lTd = lTr.find('td');
					lTd.css({
						'font-size': FONT_SIZE,
						'border': '1px solid #ADADAD',
						'padding': '2px'
					});
					
					
					$('.summary-sparkline').css({
						'text-align': 'center',
						'padding': '5px',
						'background-color': BACKGROUND_COLOR
					});
				}
				
				
				$('#actual-total-days').html(pSummaryData.actualTotalDays.toFixed(2));
				$('#addtional-days').html('-');
				$('#actual-vacation-days').html(pSummaryData.actualVacationDays.toFixed(2));
				$('#reqd-effective-hours').html(cReports.util.mFormatNumber(pSummaryData.reqdEffectiveHours.toFixed(2), true));
				$('#actual-effective-hours').html(cReports.util.mFormatNumber(pSummaryData.actualEffectiveHours.toFixed(2), true));
				$('#avg-effective-freq-label').html(LABELS.avgEffHrsReqd + '/' + pSummaryData.frequency);
				$('#reqd-effective-hours-cmp').html(cReports.util.mFormatNumber(pSummaryData.reqdAvgEffectiveHours.toFixed(2), true));
				$('#actual-effective-hours-cmp').html(cReports.util.mFormatNumber(pSummaryData.actualAvgEffectiveHours.toFixed(2), true));
				
				
				var lUser = cReports.users.mGetUserById(pSummaryData.userId);
				var lName = lUser.name;
				var lFromDate = pSummaryData.fromDate.toString(cReports.util.DATE_FORMAT);
				var lToDate = pSummaryData.toDate.toString(cReports.util.DATE_FORMAT);
				$('#summary-name').html(lName);
				$('#summary-period').html(lFromDate + ' to ' + lToDate);
				
				var lNetDiff =  pSummaryData.actualEffectiveHours - pSummaryData.reqdEffectiveHours;
				var lPercentageBarData = {
						/*excessShortage: {
							values: [{
									label: 'Excess',
									value: Math.abs(lTotalExcessDiff),
									text: cAttendance.mFormatNumber(lTotalExcessDiff),
									color: lTotalExcessDiff > 0 ? 'green' : 'red'
								},{
									label: 'Shortage',
									value: Math.abs(lTotalShortageDiff),
									text: cAttendance.mFormatNumber(lTotalShortageDiff),
									color: lTotalShortageDiff > 0 ? 'green' : 'red'
								}
							]
						},*/
						net: {
							values: [{
									label: 'Net Excess/Sortage',
									value: Math.abs(lNetDiff),
									text: cReports.util.mFormatNumber(lNetDiff),
									color: lNetDiff > 0 ? 'green' : 'red'
								}
							]
						}
				};
				this.mRenderPercentageBar(lPercentageBarData.net, 'sparkline', 'Net Excess/Shortage');
				
			}, 
			mRenderPercentageBar: function(pAttendanceData, pDivId, lLabel) {
				$('#' + pDivId).html('');
				var lWidth = 150;
				var lHeight = 20;
				var lColors = ['green', 'red'];
				
				var lElements = pAttendanceData.values;
				
				var lColorStops = '';
				var lTooltipDetails = '';
				var lTotal = 0;
				$.each(lElements, function(index, lElement) {
					lTotal = +lTotal + +lElement.value;
				});
				
				var lPercent = 0;
				$.each(lElements, function(index, lElement) {
					
					var lColor = lElement.color;//lColors[index % lColors.length];
					lColorStops = lColorStops + ', ' + lColor + ' ' + parseInt(lPercent) + '%';
					lPercent = lPercent + ((lElement.value * 100)/lTotal);
					lColorStops = lColorStops + ', ' + lColor + ' ' + parseInt(lPercent) + '%';
					
					if (lElement.value > 0) {
						lTooltipDetails = lTooltipDetails +
						  lElement.label + ': ' + '<span style="font-weight: bold;color: ' + lColor + '">' + lElement.text + '</span>'
						  + '<br/>';
					}
					
				});
				
				if (lElements.length > 1) {
					if (lTotal > 0) {
						$('#' + pDivId).html('<div class="percentage-label" style="font-size:12px;"></div><div class="percentage-bar"></div>');
						$('#' + pDivId + ' .percentage-label').html(lLabel);
						
						var lBar = $('#' + pDivId + ' .percentage-bar');
						lBar.attr('title', '');
						lBar.tooltip({
							content: function() {
								return '<div style="font-size: 12px">' + lTooltipDetails + '<div>'
							},
							track: true
						});
						lBar.css({
							width: lWidth + 'px', 
							height: lHeight + 'px',
							margin: 'auto'
						});
						
						lBar.css({
							//background: '-webkit-linear-gradient(left, red 0%,red 51%,green 51%,green 100%)'
							background: '-webkit-linear-gradient(left' + lColorStops + ')',
						});
						lBar.css({
							background: '-moz-linear-gradient(left' + lColorStops + ')'
						});
						
						lBar.css({
							background: '-o-linear-gradient(left' + lColorStops + ')'
						});
						
						lBar.css({
							background: 'linear-gradient(left' + lColorStops + ')'
						});
					}				
				} else {
					var lElement = lElements[0];
					$('#' + pDivId).prepend('<b style="font-size:12px;color: black">' + lLabel + ' : </b> <b style="color: ' + lElement.color + '">' + lElement.text + '</b>');
				}
			}
		},
		models: {
			// User Model
			user: [{
				datafield: 'userId', 
				type: 'string'
			},{
				datafield: 'name', 
				type: 'string'
			},{
				datafield: 'holidayId', 
				type: 'string'
			},{
				datafield: 'branch', 
				type: 'string'
			}],
			
			// Component Model
			component: [{
				datafield: 'type', 
				type: 'string'
			},{
				datafield: 'id', 
				type: 'string'
			},{
				datafield: 'options', 
				type: 'object'
			}],
			
			// Holiday Details Model
			holidayDetails: [{
				datafield: 'locationId', 
				type: 'number'
			},{
				datafield: 'holidaysList', 
				type: 'Array',
				fields: [{
					datafield: 'name', 
					type: 'string'
				},{
					datafield: 'date', 
					type: 'string'
				}]
			}],
			
			// Office Details Model
			officeDetails: [{
				datafield: 'locationId', 
				type: 'number'
			},{
				datafield: 'requiredHoursForFullDay', 
				type: 'string'
			},{
				datafield: 'requiredHoursForHalfDay', 
				type: 'string'
			},{
				datafield: 'shiftStart', 
				type: 'string'
			},{
				datafield: 'shiftEnd', 
				type: 'string'
			},{
				datafield: 'breakStart', 
				type: 'string'
			},{
				datafield: 'breakEnd', 
				type: 'string'
			},{
				datafield: 'doorsToConsider', 
				type: 'string'
			}],
		
			
			// To String
			toString: function(pObj) {
				var lString = '[ \n';
				$.each(pObj, function(i, lField) {
					lString = lString + '{ datafield: "' + lField.datafield + '", type: "' + lField.type + '" } \n';
				});
				
				lString = lString + ' ]';
				return lString;
			}
			
		}
	
	};
	
	/**
	 * Date and String class extensions
	 */
	Date.prototype.getWeek = function() {
	    var onejan = new Date(this.getFullYear(), 0, 1);
	    return Math.ceil((((this - onejan) / 86400000) + onejan.getDay() + 1) / 7);
	};
	
	Date.prototype.getWeek = function() {
	    var onejan = new Date(this.getFullYear(), 0, 1);
	    return Math.ceil((((this - onejan) / 86400000) + onejan.getDay() + 1) / 7);
	};
	
	String.prototype.getTimeInMillis = function() {
	    return Date.parse(this);
	};
	
	String.prototype.getDateTime = function() {
	    return new Date(Date.parse(this));
	};

})();

config = {
		endPoint: ''
		//endPoint: '/Reports/'
};

cHRDMS = {
	USERS: [{
	    "userId": "91101",
	    "name": "SENTHIL SHANMUGASUNDARAM",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91113",
	    "name": "KARTHIKEYAN VARADARAJAN",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91242",
	    "name": "GANESHA KUMAR SJ",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91394",
	    "name": "REGHUPRASAD RADHAKRISHNAN R",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91766",
	    "name": "SINDHU ISHWAR",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91772",
	    "name": "Deepashree J",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }, {
	    "userId": "91853",
	    "name": "VENKATRAMANAN K",
	    "holidayId": 1,
	    "branch": "EDST-HO"
	  }],
	OFFICE_DETAILS: [{
		locationId: 1, // Bangalore
		requiredHoursForFullDay: '8:15',
		requiredHoursForHalfDay: '4:00',
		shiftStart: '9:00',
		shiftEnd: '18:00',
		breakStart: '13:00',
		breakEnd: '14:00',
		doorsToConsider: '1,2' // 1 - HO LEFT DOOR, 2 - HO RIGHT DOOR, 3 - Server Room
	}, {
		locationId: 2, // Chennai
		requiredHoursForFullDay: '8:00',
		requiredHoursForHalfDay: '3:00',
		shiftStart: '8:00',
		shiftEnd: '16:00',
		breakStart: '12:00',
		breakEnd: '13:00',
		doorsToConsider: '1,2'
	}, {
		locationId: 8, // Hyderabad
		requiredHoursForFullDay: '9:00',
		requiredHoursForHalfDay: '5:00',
		shiftStart: '9:30',
		shiftEnd: '17:00',
		breakStart: '14:00',
		breakEnd: '14:30',
		doorsToConsider: '1,2'
	}, {
		locationId: 4, // Ahemdabad
		requiredHoursForFullDay: '8:40',
		requiredHoursForHalfDay: '4:10',
		shiftStart: '10:30',
		shiftEnd: '19:00',
		breakStart: '14:30',
		breakEnd: '15:30',
		doorsToConsider: '1,2'
	}, {
		locationId: 7, // Kochi
		requiredHoursForFullDay: '8:20',
		requiredHoursForHalfDay: '4:40',
		shiftStart: '8:30',
		shiftEnd: '17:00',
		breakStart: '13:00',
		breakEnd: '14:30',
		doorsToConsider: '1,2'
	}],
	HOLIDAYS_DETAILS: [{
		locationId: 1,
		holidaysList: [{
			name: 'Republic Day',
			date: 'Jan 26, 16'
		},{
			name: 'Ugadi',
			date: 'Apr 08, 16'
		}]
	},{
		locationId: 2,
		holidaysList: [{
			name: 'New Year Day',
			date: 'Jan 1, 16'
		},{
			name: 'Sankranti',
			date: 'Jan 15, 16'
		},{
			name: 'Republic Day',
			date: 'Jan 26, 16'
		},{
			name: 'Ugadi',
			date: 'Apr 08, 16'
		}]
	}]
	  
}


$(function () {

    /**
     * Highcharts X-range series plugin
     */
    (function (H) {
        var defaultPlotOptions = H.getOptions().plotOptions,
            columnType = H.seriesTypes.column,
            each = H.each;

        defaultPlotOptions.xrange = H.merge(defaultPlotOptions.column, {});
        H.seriesTypes.xrange = H.extendClass(columnType, {
            type: 'xrange',
            parallelArrays: ['x', 'x2', 'y'],
            requireSorting: false,
            animate: H.seriesTypes.line.prototype.animate,

            /**
             * Borrow the column series metrics, but with swapped axes. This gives free access
             * to features like groupPadding, grouping, pointWidth etc.
             */
            getColumnMetrics: function () {
                var metrics,
                    chart = this.chart;

                function swapAxes() {
                    each(chart.series, function (s) {
                        var xAxis = s.xAxis;
                        s.xAxis = s.yAxis;
                        s.yAxis = xAxis;
                    });
                }

                swapAxes();

                this.yAxis.closestPointRange = 1;
                metrics = columnType.prototype.getColumnMetrics.call(this);

                swapAxes();

                return metrics;
            },
            translate: function () {
                columnType.prototype.translate.apply(this, arguments);
                var series = this,
                    xAxis = series.xAxis,
                    metrics = series.columnMetrics;

                H.each(series.points, function (point) {
                    var barWidth = xAxis.translate(H.pick(point.x2, point.x + (point.len || 0))) - point.plotX;
                    point.shapeArgs = {
                        x: point.plotX,
                        y: point.plotY + metrics.offset,
                        width: barWidth,
                        height: metrics.width
                    };
                    point.tooltipPos[0] += barWidth / 2;
                    point.tooltipPos[1] -= metrics.width / 2;
                });
            }
        });

        /**
         * Max x2 should be considered in xAxis extremes
         */
        H.wrap(H.Axis.prototype, 'getSeriesExtremes', function (proceed) {
            var axis = this,
                dataMax = Number.MIN_VALUE;

            proceed.call(this);
            if (this.isXAxis) {
                each(this.series, function (series) {
                    each(series.x2Data || [], function (val) {
                        if (val > dataMax) {
                            dataMax = val;
                        }
                    });
                });
                if (dataMax > Number.MIN_VALUE) {
                    axis.dataMax = dataMax;
                }
            }
        });
    }(Highcharts));
});
