
(function ($) {
    "use strict";

    /*==================================================================
    [ Validate ]*/
    var input = $('.validate-input .input100');

	// 定义一个函数，用于验证表单
	function validateForm() {
		var check = true;

		for (var i = 0; i < input.length; i++) {
			if (validate(input[i]) == false) {
				showValidate(input[i]);
				check = false;
			}
		}

		return check;
	}
	
    $('.validate-form').on('submit',function(){
        return validateForm();
    });


    $('.validate-form .input100').each(function(){
        $(this).focus(function(){
           hideValidate(this);
        });
    });

    function validate (input) {
        if($(input).attr('type') == 'email' || $(input).attr('name') == 'email') {
            if($(input).val().trim().match(/^([a-zA-Z0-9_\-\.]+)@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{1,5}|[0-9]{1,3})(\]?)$/) == null) {
                return false;
            }
        }
        else {
            if($(input).val().trim() == ''){
                return false;
            }
        }
    }

    function showValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).addClass('alert-validate');
    }

    function hideValidate(input) {
        var thisAlert = $(input).parent();

        $(thisAlert).removeClass('alert-validate');
    }
    
	// 点击上传按钮时执行
	$('#loginForm').submit(function (event) {

		event.preventDefault(); // 阻止表单的默认提交行为
		let spinner = $('<span>', {
			'class': 'spinner-border text-light',
			'role': 'status',
			'aria-hidden': 'true'
		});
		let submitButton = $('#loginForm button[type="submit"]');
		// Store original button text
		let originalButtonText = submitButton.text();
		let check = validateForm();
		if(!check){
			return;
		}
		submitButton.prop('disabled', true).empty().append(spinner).append(' Loading...');
		let formData = new FormData($('#loginForm')[0]);

		$.ajax({
			url: $(this).attr('action'),
			type: 'POST',
			data: formData,
			contentType: false,
			processData: false,
			success: function (response) {
				// 上传成功
				submitButton.prop('disabled', false).empty().text(originalButtonText);
				window.location.href = window.location.origin+"/"+response.data
			},
			error: function (xhr, status, error) {
				// 上传失败
				// 在这里处理错误消息
				    // 解析JSON响应
				const response = JSON.parse(xhr.responseText);
				// 获取message属性的值
				const message = response.message;
				let errorMessage;
				switch (response.status) {
				  case 1001:
					errorMessage = $('#errorUser');
					errorMessage.text(message);
					break;
				  default:
				  	errorMessage = $('#errorPassword');
					errorMessage.text(message);
				}
				submitButton.prop('disabled', false).empty().text(originalButtonText);
			}
		});
	});
    
})(jQuery);