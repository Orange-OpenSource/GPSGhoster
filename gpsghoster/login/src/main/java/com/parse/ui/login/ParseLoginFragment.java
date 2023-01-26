/*
 *  Copyright (c) 2014, Parse, LLC. All rights reserved.
 *
 *  You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 *  copy, modify, and distribute this software in source code or binary form for use
 *  in connection with the web services and APIs provided by Parse.
 *
 *  As with any software that integrates with the Parse platform, your use of
 *  this software is subject to the Parse Terms of Service
 *  [https://www.parse.com/about/terms]. This copyright notice shall be
 *  included in all copies or substantial portions of the software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.parse.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.orange.common.app.IApp;
import com.orange.common.biometric.BiometricAuthListener;
import com.orange.common.biometric.BiometricUtil;
import com.orange.common.crypto.CryptographyUtil;
import com.orange.common.crypto.EncryptedMessage;
import com.orange.gpsghoster.util.CommonUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.facebook.ParseFacebookUtils;
import com.parse.twitter.ParseTwitterUtils;
import com.parse.twitter.Twitter;
import com.parse.ui.R;

import org.json.JSONObject;

import javax.crypto.Cipher;

/**
 * Fragment for the user login screen.
 */
public class ParseLoginFragment extends ParseLoginFragmentBase {

  public interface ParseLoginFragmentListener {
    void onSignUpClicked(String username, String password);

    void onLoginHelpClicked();

    void onLoginSuccess();
  }

  private static final String LOG_TAG = "ParseLoginFragment";
  private static final String USER_OBJECT_NAME_FIELD = "name";

  private View parseLogin;
  private EditText usernameField;
  private EditText passwordField;
  private TextView parseLoginHelpButton;
  private Button parseLoginButton;
  private ImageButton parseLoginImageButton;
  private Button parseSignupButton;
  private Button facebookLoginButton;
  private Button twitterLoginButton;
  private ParseLoginFragmentListener loginFragmentListener;
  private ParseOnLoginSuccessListener onLoginSuccessListener;

  private ParseLoginConfig config;

  public static ParseLoginFragment newInstance(Bundle configOptions) {
    ParseLoginFragment loginFragment = new ParseLoginFragment();
    loginFragment.setArguments(configOptions);
    return loginFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup parent,
                           Bundle savedInstanceState) {
    config = ParseLoginConfig.fromBundle(getArguments(), getActivity());

    View v = inflater.inflate(R.layout.com_parse_ui_parse_login_fragment,
        parent, false);
    ImageView appLogo = v.findViewById(R.id.app_logo);
    parseLogin = v.findViewById(R.id.parse_login);
    usernameField = v.findViewById(R.id.login_username_input);
    passwordField = v.findViewById(R.id.login_password_input);
    parseLoginHelpButton = (Button) v.findViewById(R.id.parse_login_help);
    parseLoginButton = v.findViewById(R.id.parse_login_button);
    parseLoginImageButton = v.findViewById(R.id.parse_login_imagebutton);
    parseSignupButton = v.findViewById(R.id.parse_signup_button);
    facebookLoginButton = v.findViewById(R.id.facebook_login);
    twitterLoginButton = v.findViewById(R.id.twitter_login);

    if (appLogo != null && config.getAppLogo() != null) {
      appLogo.setImageResource(config.getAppLogo());
    }
    if (allowParseLoginAndSignup()) {
      setUpParseLoginAndSignup();
    }
    if (allowFacebookLogin()) {
      setUpFacebookLogin();
    }
    if (allowTwitterLogin()) {
      setUpTwitterLogin();
    }
    return v;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    final Activity activity = getActivity();
    if (activity instanceof ParseLoginFragmentListener) {
      loginFragmentListener = (ParseLoginFragmentListener) activity;
    } else {
      throw new IllegalArgumentException(
          "Activity must implemement ParseLoginFragmentListener");
    }

    if (activity instanceof ParseOnLoginSuccessListener) {
      onLoginSuccessListener = (ParseOnLoginSuccessListener) activity;
    } else {
      throw new IllegalArgumentException(
          "Activity must implemement ParseOnLoginSuccessListener");
    }

    if (activity instanceof ParseOnLoadingListener) {
      onLoadingListener = (ParseOnLoadingListener) activity;
    } else {
      throw new IllegalArgumentException(
          "Activity must implemement ParseOnLoadingListener");
    }
  }

  @Override
  protected String getLogTag() {
    return LOG_TAG;
  }

  private void setUpParseLoginAndSignup() {
    Context context = this.getContext();

    parseLogin.setVisibility(View.VISIBLE);

    if (config.isParseLoginEmailAsUsername()) {
      usernameField.setHint(R.string.com_parse_ui_email_input_hint);
      usernameField.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    if (config.getParseLoginButtonText() != null) {
      parseLoginButton.setText(config.getParseLoginButtonText());
    }

    parseLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if (username.length() == 0) {
          if (config.isParseLoginEmailAsUsername()) {
            showToast(R.string.com_parse_ui_no_email_toast);
          } else {
            showToast(R.string.com_parse_ui_no_username_toast);
          }
        } else if (password.length() == 0) {
          showToast(R.string.com_parse_ui_no_password_toast);
        } else {
          if (BiometricUtil.isBiometricReady(context)) {
            showBiometricPromptToEncrypt();
          } else {
            showAlertToSetupBiometric();
          }
        }
      }
    });

    if (config.getParseSignupButtonText() != null) {
      parseSignupButton.setText(config.getParseSignupButtonText());
    }

    parseSignupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        loginFragmentListener.onSignUpClicked(username, password);
      }
    });

    if (config.getParseLoginHelpText() != null) {
      parseLoginHelpButton.setText(config.getParseLoginHelpText());
    }

    parseLoginHelpButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        loginFragmentListener.onLoginHelpClicked();
      }
    });

    parseLoginImageButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (BiometricUtil.isBiometricReady(context)) {
          showBiometricPromptToDecrypt();
        } else {
          showAlertToSetupBiometric();
        }
      }
    });
  }


  private LogInCallback facebookLoginCallbackV4 = new LogInCallback() {
    @Override
    public void done(ParseUser user, ParseException e) {
      if (isActivityDestroyed()) {
        return;
      }

      if (user == null) {
        loadingFinish();
        if (e != null) {
          showToast(R.string.com_parse_ui_facebook_login_failed_toast);
          debugLog(getString(R.string.com_parse_ui_login_warning_facebook_login_failed) +
                  e.toString());
        }
      } else if (user.isNew()) {
        GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
            new GraphRequest.GraphJSONObjectCallback() {
              @Override
              public void onCompleted(JSONObject fbUser,
                                      GraphResponse response) {
                  /*
                    If we were able to successfully retrieve the Facebook
                    user's name, let's set it on the fullName field.
                  */
                ParseUser parseUser = ParseUser.getCurrentUser();
                if (fbUser != null && parseUser != null
                        && fbUser.optString("name").length() > 0) {
                  parseUser.put(USER_OBJECT_NAME_FIELD, fbUser.optString("name"));
                  parseUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                      if (e != null) {
                        debugLog(getString(
                                R.string.com_parse_ui_login_warning_facebook_login_user_update_failed) +
                                e.toString());
                      }
                      loginSuccess();
                    }
                  });
                }
                loginSuccess();
              }
            }
        ).executeAsync();
      } else {
        loginSuccess();
      }
    }
  };

  private void setUpFacebookLogin() {
    facebookLoginButton.setVisibility(View.VISIBLE);

    if (config.getFacebookLoginButtonText() != null) {
      facebookLoginButton.setText(config.getFacebookLoginButtonText());
    }

    facebookLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        loadingStart(false); // Facebook login pop-up already has a spinner
        if (config.isFacebookLoginNeedPublishPermissions()) {
          ParseFacebookUtils.logInWithPublishPermissionsInBackground(getActivity(),
                  config.getFacebookLoginPermissions(), facebookLoginCallbackV4);
        } else {
          ParseFacebookUtils.logInWithReadPermissionsInBackground(getActivity(),
                  config.getFacebookLoginPermissions(), facebookLoginCallbackV4);
        }
      }
    });
  }

  private void setUpTwitterLogin() {
    twitterLoginButton.setVisibility(View.VISIBLE);

    if (config.getTwitterLoginButtonText() != null) {
      twitterLoginButton.setText(config.getTwitterLoginButtonText());
    }

    twitterLoginButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        loadingStart(false); // Twitter login pop-up already has a spinner
        ParseTwitterUtils.logIn(getActivity(), new LogInCallback() {
          @Override
          public void done(ParseUser user, ParseException e) {
            if (isActivityDestroyed()) {
              return;
            }

            if (user == null) {
              loadingFinish();
              if (e != null) {
                showToast(R.string.com_parse_ui_twitter_login_failed_toast);
                debugLog(getString(R.string.com_parse_ui_login_warning_twitter_login_failed) +
                    e.toString());
              }
            } else if (user.isNew()) {
              Twitter twitterUser = ParseTwitterUtils.getTwitter();
              if (twitterUser != null
                  && twitterUser.getScreenName().length() > 0) {
                /*
                  To keep this example simple, we put the users' Twitter screen name
                  into the name field of the Parse user object. If you want the user's
                  real name instead, you can implement additional calls to the
                  Twitter API to fetch it.
                */
                user.put(USER_OBJECT_NAME_FIELD, twitterUser.getScreenName());
                user.saveInBackground(new SaveCallback() {
                  @Override
                  public void done(ParseException e) {
                    if (e != null) {
                      debugLog(getString(
                          R.string.com_parse_ui_login_warning_twitter_login_user_update_failed) +
                          e.toString());
                    }
                    loginSuccess();
                  }
                });
              }
            } else {
              loginSuccess();
            }
          }
        });
      }
    });
  }

  private boolean allowParseLoginAndSignup() {
    if (!config.isParseLoginEnabled()) {
      return false;
    }

    if (usernameField == null) {
      debugLog(R.string.com_parse_ui_login_warning_layout_missing_username_field);
    }
    if (passwordField == null) {
      debugLog(R.string.com_parse_ui_login_warning_layout_missing_password_field);
    }
    if (parseLoginButton == null) {
      debugLog(R.string.com_parse_ui_login_warning_layout_missing_login_button);
    }
    if (parseSignupButton == null) {
      debugLog(R.string.com_parse_ui_login_warning_layout_missing_signup_button);
    }
    if (parseLoginHelpButton == null) {
      debugLog(R.string.com_parse_ui_login_warning_layout_missing_login_help_button);
    }

    boolean result = (usernameField != null) && (passwordField != null)
        && (parseLoginButton != null) && (parseSignupButton != null)
        && (parseLoginHelpButton != null);

    if (!result) {
      debugLog(R.string.com_parse_ui_login_warning_disabled_username_password_login);
    }
    return result;
  }

  private boolean allowFacebookLogin() {
    if (!config.isFacebookLoginEnabled()) {
      return false;
    }

    if (facebookLoginButton == null) {
      debugLog(R.string.com_parse_ui_login_warning_disabled_facebook_login);
      return false;
    } else {
      return true;
    }
  }

  private boolean allowTwitterLogin() {
    if (!config.isTwitterLoginEnabled()) {
      return false;
    }

    if (twitterLoginButton == null) {
      debugLog(R.string.com_parse_ui_login_warning_disabled_twitter_login);
      return false;
    } else {
      return true;
    }
  }

  private void loginSuccess() {
    onLoginSuccessListener.onLoginSuccess();
  }

  private void computeCredentialKey(@NonNull BiometricPrompt.AuthenticationResult result) {
    Cipher cipher = result.getCryptoObject().getCipher();
    String username = usernameField.getText().toString();
    String password = passwordField.getText().toString();
    Credential credential = new Credential(username, password);
    String plainTextMessage = new Gson().toJson(credential);
    EncryptedMessage encryptedMessage = CryptographyUtil.encryptData(plainTextMessage, cipher);
    String json = new Gson().toJson(encryptedMessage);

    ((ParseLoginActivity) this.getActivity()).getPreferenceDataStore().putString(this.getResources().getString(R.string.credential_key), json);
    CommonUtils.displayToast(this.getContext(), "credential saved"); // TODO add string resource
  }

  private void computeDatabasePasswordKey(String username, String password) {
    String packageName = getActivity().getPackageName();
    ParseUser currentUser = ParseUser.getCurrentUser();
    String email = currentUser.getEmail();
    String userId = currentUser.getObjectId();
    String sessionToken = currentUser.getSessionToken();
    String input = packageName + ":" + username + ":" + password + ":" + email + ":" + userId + ":" + sessionToken;
    byte[] keyBytes = CryptographyUtil.computeKey(input);
    String keyString = CryptographyUtil.bytesToHex(keyBytes);

    ((IApp) this.getActivity().getApplication()).saveDatabasePassword(keyString);
  }

  private void showBiometricPromptToEncrypt() {
    Context context = this.getContext();
    // Create Cryptography Object
    BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(
            CryptographyUtil.getInitializedCipherForEncryption()
    );
    // Show BiometricPrompt
    BiometricUtil.showBiometricPrompt(
      "Biometric Authentication",
      "Enter biometric credentials to proceed.",
      "Input your Fingerprint or FaceID to ensure it's you!",
      (AppCompatActivity) this.getActivity(),
      new BiometricAuthListener() {

        @Override
        public void onBiometricAuthenticationError(int errorCode, @NonNull String errorMessage) {
          CommonUtils.displayToast(context, "Biometric error: $errorMessage");
        }

        @Override
        public void onBiometricAuthenticationSuccess(@NonNull BiometricPrompt.AuthenticationResult result) {
          String username = usernameField.getText().toString();
          String password = passwordField.getText().toString();

          loadingStart(true);
          ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
              if (isActivityDestroyed()) {
                return;
              }

              if (user != null) {
                if (user.getBoolean("emailVerified")) {
                  loadingFinish();
                  computeCredentialKey(result);
                  computeDatabasePasswordKey(username, password);
                  loginSuccess();
                } else {
                  loadingFinish();
                  showToast(R.string.com_parse_ui_parse_email_not_verified_toast);
                }
              } else {
                loadingFinish();
                if (e != null) {
                  debugLog(getString(R.string.com_parse_ui_login_warning_parse_login_failed) +
                          e.toString());
                  if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    if (config.getParseLoginInvalidCredentialsToastText() != null) {
                      showToast(config.getParseLoginInvalidCredentialsToastText());
                    } else {
                      showToast(R.string.com_parse_ui_parse_login_invalid_credentials_toast);
                    }
                    passwordField.selectAll();
                    passwordField.requestFocus();
                  } else if (e.getCode() == ParseException.EMAIL_NOT_FOUND) {
                    showToast(R.string.com_parse_ui_parse_email_not_verified_toast);
                  } else {
                    showToast(R.string.com_parse_ui_parse_login_failed_unknown_toast);
                  }
                }
              }
            }
          });
        }
      },
      cryptoObject,
      true
    );
  }

  private EncryptedMessage encryptedMessage;

  private void showBiometricPromptToDecrypt() {
    Context context = this.getContext();
    String credential = ((ParseLoginActivity) this.getActivity()).getPreferenceDataStore().getString(this.getActivity().getResources().getString(R.string.credential_key), null);
    encryptedMessage = new Gson().fromJson(credential, EncryptedMessage.class);
    if (encryptedMessage == null) {
      CommonUtils.displayToast(context, "Biometric error: no credential available.");
      return ;
    }
    // Retrieve Cryptography Object
    BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(
      CryptographyUtil.getInitializedCipherForDecryption(encryptedMessage.getInitializationVector()));
    // Show BiometricPrompt With Cryptography Object
    BiometricUtil.showBiometricPrompt(
      "Biometric Authentication",
      "Enter biometric credentials to proceed.",
      "Input your Fingerprint or FaceID to ensure it's you!",
      (AppCompatActivity) this.getActivity(),
      new BiometricAuthListener() {

        @Override
        public void onBiometricAuthenticationError(int errorCode, @NonNull String errorMessage) {
          CommonUtils.displayToast(context, "Biometric error: $errorMessage");
        }

        @Override
        public void onBiometricAuthenticationSuccess(@NonNull BiometricPrompt.AuthenticationResult result) {
          String decryptedMessage = CryptographyUtil.decryptData(encryptedMessage.getCipherText(), result.getCryptoObject().getCipher());
          Credential credential = new Gson().fromJson(decryptedMessage, Credential.class);
          encryptedMessage = null;

          String username = credential.getUsername();
          String password = credential.getPassword();

          loadingStart(true);
          ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
              if (isActivityDestroyed()) {
                return;
              }

              if (user != null) {
                if (user.getBoolean("emailVerified")) {
                  loadingFinish();
                  computeDatabasePasswordKey(username, password);
                  loginSuccess();
                } else {
                  loadingFinish();
                  showToast(R.string.com_parse_ui_parse_email_not_verified_toast);
                }
              } else {
                loadingFinish();
                if (e != null) {
                  debugLog(getString(R.string.com_parse_ui_login_warning_parse_login_failed) +
                          e.toString());
                  if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    if (config.getParseLoginInvalidCredentialsToastText() != null) {
                      showToast(config.getParseLoginInvalidCredentialsToastText());
                    } else {
                      showToast(R.string.com_parse_ui_parse_login_invalid_credentials_toast);
                    }
                    passwordField.selectAll();
                    passwordField.requestFocus();
                  } else if (e.getCode() == ParseException.EMAIL_NOT_FOUND) {
                    showToast(R.string.com_parse_ui_parse_email_not_verified_toast);
                  } else {
                    showToast(R.string.com_parse_ui_parse_login_failed_unknown_toast);
                  }
                }
              }
            }
          });
        }
      },
      cryptoObject,
      true);
  }

  private void showAlertToSetupBiometric() {
    CommonUtils.displayMessage(
            this.getContext(),
            getString(R.string.message_encryption_failed),
            getString(R.string.message_no_biometric),
            (DialogInterface.OnClickListener) (dialog, which) -> {
              BiometricUtil.lunchBiometricSettings(this.getContext());
            });
  }
}
