package com.ajwalker.controller;

import com.ajwalker.constant.ReactApis;
import com.ajwalker.dto.request.*;
import com.ajwalker.dto.response.*;
import com.ajwalker.entity.User;
import com.ajwalker.exception.ErrorType;
import com.ajwalker.exception.HRAppException;
import com.ajwalker.service.UserAuthVerifyCodeService;
import com.ajwalker.service.UserService;
import com.ajwalker.utility.Enum.user.EUserState;
import com.ajwalker.utility.JwtManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.ajwalker.constant.RestApis.*;

@RestController
@RequestMapping(USER)
@RequiredArgsConstructor
@CrossOrigin("*")  //Frontend isteklerini sınırlar, * koyunca her porttan gelen istekleri kabul eder
public class UserController {
	private final UserService userService;
	private final UserAuthVerifyCodeService userAuthVerifyCodeService;
	private final JwtManager jwtManager;
	
	@PostMapping(REGISTER)
	public ResponseEntity<BaseResponse<Boolean>> register(@RequestBody @Valid RegisterRequestDto dto) {
		if (!dto.password().equals((dto.rePassword()))) throw new HRAppException(ErrorType.PASSWORD_ERROR);
		
		return ResponseEntity.ok(BaseResponse.<Boolean>builder().code(200).data(userService.register(dto))
		                                     .message("Uyelik islemini tamamlamak icin mail onayinizi bekliyoruz!")
		                                     .success(true).build());
	}
	
	@GetMapping(AUTHMAIL)
	public ResponseEntity<Void> authUser(@RequestParam(name = "auth") String authCode) {
		Optional<Long> userIdOptional = userAuthVerifyCodeService.findUserIdByAuthCode(authCode);
		if (userIdOptional.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		
		userService.updateUserStateToInReView(userIdOptional.get());
		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(URI.create(ReactApis.DOMAIN));
		return new ResponseEntity<>(headers, HttpStatus.FOUND);
	}
	
	@PostMapping(DOLOGIN)
	public ResponseEntity<BaseResponse<LoginResponseDto>> doLogin(@RequestBody @Valid DologinRequestDto dto) {
		
		return ResponseEntity.ok(BaseResponse.<LoginResponseDto>builder().success(true).message("Giriş işlemi " +
				                                                                                        "başarılı")
		                                     .data(userService.doLogin(dto)).code(200).build());
	}
	
	@PostMapping(FORGOT_PASSWORD_MAIL)
	public ResponseEntity<BaseResponse<Boolean>> forgotPasswordMail(@RequestBody ForgotPasswordRequestDto dto) {
		return ResponseEntity.ok(BaseResponse.<Boolean>builder().success(true)
		                                     .message("Yeni sifre olusturma linki mail adresine gonderilmistir!")
		                                     .data(userService.forgotPasswordMail(dto.forgotPasswordEmail())).code(200)
		                                     .build());
	}
	
	@GetMapping(NEW_PASSWORD)
	public RedirectView setNewPassword(@RequestParam(name = "auth") String authCode) {
		Optional<Long> userIdOptional = userAuthVerifyCodeService.findUserIdByAuthCode(authCode);
		if (userIdOptional.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		
		return new RedirectView(ReactApis.NEW_PASSWORD_PAGE + "?code=" + authCode);
		
	}
	
	@PostMapping(NEW_PASSWORD)
	public ResponseEntity<BaseResponse<Boolean>> setNewPassword(@RequestBody @Valid NewPasswordRequestDto dto) {
		if (!dto.password().equals(dto.rePassword())) {
			throw new HRAppException(ErrorType.PASSWORD_ERROR);
		}
		
		return ResponseEntity.ok(BaseResponse.<Boolean>builder().success(true)
		                                     .message("Yeni sifre basiriyla olsuturuldu!")
		                                     .data(userService.updateUserPassword(dto)).code(200).build());
	}
	
	@GetMapping(GETUSERPROFILEINFO)
	public ResponseEntity<BaseResponse<GetUserProfileInfoDto>> getUserProfileInfo(@RequestParam(name = "token") String token) {
		Optional<Long> optionalUserId = jwtManager.verifyToken(token);
		if (optionalUserId.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		return ResponseEntity.ok(BaseResponse.<GetUserProfileInfoDto>builder()
		                                     .message("Kullanıcı bilgisi Listelendi")
		                                     .code(200)
		                                     .success(true)
		                                     .data(userService.getUserProfileInfo(optionalUserId.get()))
		                                     .build());
		
	}

	@PostMapping(CREATE_HOLIDAY)
	public ResponseEntity<BaseResponse<Boolean>> createWorkHoliday(@RequestBody @Valid WorkHolidayRequestDto dto) {
		Optional<Long> optionalUserId = jwtManager.verifyToken(dto.token());
		if (optionalUserId.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		return ResponseEntity.ok(BaseResponse.<Boolean>builder().success(true)
		                                     .message("İzin oluşturuldu")
		                                     .code(200)
		                                     .data(userService.createWorkHoliday(dto, optionalUserId.get()))
		                                     .build());
		
	}
	
	@PostMapping(USER_INFORMATION)
	public ResponseEntity<BaseResponse<Boolean>> updateUserInformation(@RequestBody @Valid FirstLoginInformationDto dto) {
		Optional<Long> optionalUserId = jwtManager.verifyToken(dto.token());
		if (optionalUserId.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		return ResponseEntity.ok(BaseResponse.<Boolean>builder().success(true)
				.message("bilgiler güncellendi")
				.code(200)
				.data(userService.updateUserInformation(dto, optionalUserId.get()))
				.build());

	}
	
	@PutMapping(USER_UPDATE_INFORMATION)
	public ResponseEntity<BaseResponse<GetUserProfileInfoDto>> updateUserProfileInformation(@RequestBody @Valid UpdateUserProfileInformationRequestDto dto){
		return ResponseEntity.ok(BaseResponse.<GetUserProfileInfoDto>builder()
				                         .message("Personel bilgileri başarıyla Güncellendi")
				                         .code(200)
				                         .success(true)
				                         .data(userService.updateUserProfile(dto))
		                                     .build());
	}
	
	@GetMapping(GETALLVIEWUSERPERMIT)
	public ResponseEntity<BaseResponse<List<ViewYourUserPermitResponseDto>>> viewYourUserPermit(@RequestParam(name =
			"token") String token) {
		Optional<Long> optionalUserId = jwtManager.verifyToken(token);
		if (optionalUserId.isEmpty()) {
			throw new HRAppException(ErrorType.NOTFOUND_USER);
		}
		
		return  ResponseEntity.ok(BaseResponse.<List<ViewYourUserPermitResponseDto>>builder()
		                                      .message("İzinler başarıyla listelendi")
				                          .code(200)
				                          .success(true)
				                          .data(userService.viewYourUserPermit(optionalUserId.get()))
				                          .build());
	}
	
	
}