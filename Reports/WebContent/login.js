
	cLogin = {
		mDoLogin: function(pUsername, pPassword) {
			var lSession = window.sessionStorage;
			if (lSession)
			var lLoginDetails = {
				username: pUsername,
				password: pPassword
			};
			$.ajax({
				url: config.endPoint + 'api/v1/auth/login',
				method: 'post',
				data: lLoginDetails,
				dataType: 'json',
				contentType: 'application/json',
				success: function(response) {
					
					lSession.setItem('login', response);
					if (response) {
						cLogin.mRedirectPage("/ReportsA/team_report.html");
					} else {
						alert('Invalid username or password');
					}
					
					console.log(response);
				},
				error: function() {
					console.log('Error while login');
				}
				
			});
		},
		mSubmitLogin: function() {
			var lUsername = $('#username').val();
			var lPassword = $('#password').val();
			this.mDoLogin(lUsername, lPassword);
		},
		mDoLogout: function() {
			var lSession = window.sessionStorage;
			lSession.setItem('login', false);
			cLogin.mRedirectPage("/ReportsA/login.html");
		},
		mSetState: function(pStateType) {
			
		},
		mRedirectPage: function(pPageURL) {
			window.location.href = pPageURL;
		}
	}
	
	$(document).ready(function() {
		var lSession = window.sessionStorage;
		var lIsLoggedIn = lSession.getItem('login');
		if (lIsLoggedIn === 'false' && window.location.pathname !== '/Reports') {
			/*cLogin.mRedirectPage("/ReportsA/team_report.html");*/
			//cLogin.mRedirectPage("/ReportsA");
		}
	});