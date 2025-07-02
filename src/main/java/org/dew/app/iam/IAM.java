package org.dew.app.iam;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.web.reactive.function.client.WebClient;

/*

Filter:

@Override
public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
  throws IOException, ServletException {
  
  String servletPath = request.getServletPath();
  // Exclude logout and resources URL
  if(servletPath == null || servletPath.equals("/logout") || servletPath.indexOf('.') > 0) {
    chain.doFilter(request, response);
    return;
  }
  
  // Check Session
  HttpSession session = request.getSession();
  String sessionUser = (String) session.getAttribute(SESS_USER);
  if(sessionUser == null || sessionUser.length() == 0) {
    // Check last authorize request (to avoid the loop)
    String lastAuthorizeRequest = (String) session.getAttribute(SESS_AUTH);
    if(lastAuthorizeRequest == null || lastAuthorizeRequest.length() == 0) {
      String location = IAM.getAuthorizeRequest(servletPath);
      if(location != null && location.length() > 0) {
        session.setAttribute(SESS_AUTH, location);
        
        response.setHeader("Location", location);
        response.sendError(302);
        return;
      }
    }
  }
  chain.doFilter(request, response);
}

Controller:

@GetMapping("/iam")
public String iam(
    @RequestParam(name = "code",              required = false) String code,
    @RequestParam(name = "state",             required = false) String state, 
    @RequestParam(name = "session_state",     required = false) String session_state,
    @RequestParam(name = "error",             required = false) String error,
    @RequestParam(name = "error_description", required = false) String error_description,
    Model model, 
    HttpServletRequest request) {
  
  System.out.println("/iam code=" + code + ",state=" + state + ",session_state=" + session_state + "," + error + "," + error_description);
  
  IAMTokenResponse tokenResponse = IAM.requestToken(code, state);
  
  if(tokenResponse != null) {
    String token = tokenResponse.getAccess_token();
    if(token != null && token.length() > 0) {
      String subject = IAM.getSubject(token);
      
      if(subject != null && subject.length() > 0) {
        HttpSession session = request.getSession();
        session.setAttribute(SESS_USER,     subject);
        session.setAttribute(SESS_TOKEN,    token);
        session.setAttribute(SESS_STATE,    state);
        session.setAttribute(SESS_ID_TOKEN, tokenResponse.getId_token());
        
        String servletPath = IAM.getServletPath(state);
        if(servletPath != null && servletPath.length() > 0 && servletPath.startsWith("/")) {
          return "redirect:" + servletPath;
        }
      }
    }
  }
  return "home";
}

@GetMapping("/logout")
public String logout(Model model, HttpServletRequest request, HttpServletResponse response) {
  HttpSession session = request.getSession();
  
  String idToken = (String) session.getAttribute(SESS_ID_TOKEN);
  String state   = (String) session.getAttribute(SESS_STATE);
  
  System.out.println("/logout idToken=" + idToken + ",state=" + state);
  
  session.removeAttribute(SESS_AUTH);
  session.removeAttribute(SESS_USER);
  session.removeAttribute(SESS_TOKEN);
  session.removeAttribute(SESS_STATE);
  session.removeAttribute(SESS_ID_TOKEN);
  session.removeAttribute(SESS_USER_DTO);
  
  if(idToken != null && idToken.length() > 0) {
    if(state != null && state.length() > 0) {
      String logoutURL = IAM.getLogoutURL(idToken, state);
      if(logoutURL != null && logoutURL.length() > 0) {
        return "redirect:" + logoutURL;
      }
    }
  }
  return "logout";
}
*/
public class IAM {
  
  private static List<String>        listStates      = new ArrayList<>();
  private static Map<String, String> mapStatesCodVer = new HashMap<>();
  private static Map<String, String> mapStatesPath   = new HashMap<>();
  private static final int           MAX_SIZE_STATES = 2000;
  
  // IAM Configuration
  public static final String IAM_URL_AUTHORIZE = "IAM_URL_AUTHORIZE";
  public static final String IAM_URL_TOKEN     = "IAM_URL_TOKEN";
  public static final String IAM_URL_USERINFO  = "IAM_URL_USERINFO";
  public static final String IAM_URL_LOGOUT    = "IAM_URL_LOGOUT";
  public static final String IAM_CLIENT_ID     = "IAM_CLIENT_ID";
  public static final String IAM_REDIRECT_URI  = "IAM_REDIRECT_URI";
  public static final String IAM_POST_LOGOUT   = "IAM_POST_LOGOUT";
  public static final String IAM_SCOPE         = "IAM_SCOPE";
  // Defaults
  public static final String DEF_URL_AUTHORIZE = "";
  public static final String DEF_URL_TOKEN     = "";
  public static final String DEF_URL_USERINFO  = "";
  public static final String DEF_URL_LOGOUT    = "";
  public static final String DEF_CLIENT_ID     = "";
  public static final String DEF_REDIRECT_URI  = "http://localhost:8080/iam";
  public static final String DEF_POST_LOGOUT   = "http://localhost:8080/logout";
  
  public static String getAuthorizeRequest(String path) {
    System.out.println("IAM.getAuthorizeRequest(" + path + ")...");
    String iamURLAuth = System.getenv(IAM_URL_AUTHORIZE);
    if(iamURLAuth == null || iamURLAuth.length() == 0) {
      iamURLAuth = DEF_URL_AUTHORIZE;
    }
    if(iamURLAuth == null || iamURLAuth.length() < 8) {
      System.out.println("IAM.getAuthorizeRequest(" + path + ") -> null (" + IAM_URL_AUTHORIZE + "=" + iamURLAuth + ")");
      return null;
    }
    String iamClientId    = System.getenv(IAM_CLIENT_ID);
    String iamScope       = System.getenv(IAM_SCOPE);
    String iamRedirectURI = System.getenv(IAM_REDIRECT_URI);
    
    if(iamClientId == null || iamClientId.length() == 0) {
      iamClientId = DEF_CLIENT_ID;
    }
    if(iamClientId == null || iamClientId.length() < 3) {
      System.out.println("IAM.getAuthorizeRequest(" + path + ") -> null (" + IAM_CLIENT_ID + "=" + iamClientId + ")");
      return null;
    }
    if(iamScope == null || iamScope.length() == 0) {
      iamScope = "openid";
    }
    if(iamRedirectURI == null || iamRedirectURI.length() == 0) {
      iamRedirectURI = DEF_REDIRECT_URI;
    }
    String state         = generateState();
    String codeVerifier  = generateCodeVerifier();
    String codeChallenge = generateCodeChallange(codeVerifier);
    
    if(listStates.size() >= MAX_SIZE_STATES) {
      String state0 = listStates.remove(0);
      mapStatesCodVer.remove(state0);
      mapStatesPath.remove(state0);
    }
    if(path == null) path = "";
    
    // Authorization request
    String result = iamURLAuth + "?response_type=code&";
    try {
      result += "client_id="        + URLEncoder.encode(iamClientId,    "UTF-8") + "&";
      result += "scope="            + URLEncoder.encode(iamScope,       "UTF-8") + "&";
      if(iamRedirectURI != null && iamRedirectURI.length() > 0) {
        result += "redirect_uri=" + URLEncoder.encode(iamRedirectURI, "UTF-8") + "&";
      }
      result += "state="          + state         + "&";
      result += "code_challenge=" + codeChallenge + "&";
      result += "code_challenge_method=S256";
    }
    catch(Exception ex) {
      System.out.println("IAM.getAuthorizeRequest(" + path + ") -> null (Exception: " + ex + ")");
      return null;
    }
    
    listStates.add(state);
    mapStatesCodVer.put(state, codeVerifier);
    mapStatesPath.put(state, path);
    
    System.out.println("IAM.getAuthorizeRequest(" + path + ") -> " + result);
    return result;
  }
  
  public static String getLogoutURL(String idToken, String state) {
    System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ")...");
    if(idToken == null || idToken.length() == 0) {
      System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> null (idToken=" + idToken + ")");
      return null;
    }
    if(state == null || state.length() == 0) {
      System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> null (state=" + state + ")");
      return null;
    }
    String iamURLLogout = System.getenv(IAM_URL_LOGOUT);
    if(iamURLLogout == null || iamURLLogout.length() == 0) {
      iamURLLogout = DEF_URL_LOGOUT;
    }
    if(iamURLLogout == null || iamURLLogout.length() < 8) {
      System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> null (" + IAM_URL_LOGOUT + "=" + iamURLLogout + ")");
      return null;
    }
    String iamClientId   = System.getenv(IAM_CLIENT_ID);
    String iamPostLogout = System.getenv(IAM_POST_LOGOUT);
    if(iamPostLogout == null || iamPostLogout.length() == 0) {
      iamPostLogout = DEF_POST_LOGOUT;
    }
    if(iamClientId == null || iamClientId.length() == 0) {
      iamClientId = DEF_CLIENT_ID;
    }
    if(iamClientId == null || iamClientId.length() < 3) {
      System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> null (" + IAM_CLIENT_ID + "=" + iamClientId + ")");
      return null;
    }
    
    // Authorization request
    String result = iamURLLogout + "?";
    try {
      result += "id_token_hint="  + URLEncoder.encode(idToken,     "UTF-8") + "&";
      result += "client_id="      + URLEncoder.encode(iamClientId, "UTF-8") + "&";
      if(iamPostLogout != null && iamPostLogout.length() > 0) {
        result += "post_logout_redirect_uri="  + URLEncoder.encode(iamPostLogout, "UTF-8") + "&";
      }
      result += "state="          + state;
    }
    catch(Exception ex) {
      System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> null (Exception: " + ex + ")");
      return null;
    }
    
    listStates.remove(state);
    mapStatesCodVer.remove(state);
    mapStatesPath.remove(state);
    
    System.out.println("IAM.getLogoutURL(" + idToken + "," + state + ") -> " + result);
    return result;
  }
  
  public static IAMTokenResponse requestToken(String code, String state) {
    System.out.println("IAM.requestToken(" + code + "," + state + ")...");
    if(code == null || code.length() == 0) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (invalid code)");
      return null;
    }
    if(state == null || state.length() == 0) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (invalid state)");
      return null;
    }
    String codeVerifier = mapStatesCodVer.get(state);
    if(codeVerifier == null || codeVerifier.length() == 0) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (no codeVerifier for state " + state + ")");
      return null;
    }
    String iamClientId = System.getenv(IAM_CLIENT_ID);
    if(iamClientId == null || iamClientId.length() == 0) {
      iamClientId = DEF_CLIENT_ID;
    }
    if(iamClientId == null || iamClientId.length() < 3) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (IAM_CLIENT_ID=" + iamClientId + ")");
      return null;
    }
    String iamRedirectURI = System.getenv(IAM_REDIRECT_URI);
    if(iamRedirectURI == null || iamRedirectURI.length() == 0) {
      iamRedirectURI = DEF_REDIRECT_URI;
    }
    String iamURLToken = System.getenv(IAM_URL_TOKEN);
    if(iamURLToken == null || iamURLToken.length() == 0) {
      iamURLToken = DEF_URL_TOKEN;
    }
    if(iamURLToken == null || iamURLToken.length() < 8) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (" + IAM_URL_TOKEN + "=" + iamURLToken + ")");
      return null;
    }
    String baseUrl = null;
    String postUri = null;
    int lastSep = iamURLToken.lastIndexOf('/');
    if(lastSep > 0) {
      baseUrl = iamURLToken.substring(0, lastSep);
      postUri = iamURLToken.substring(lastSep);
    }
    else {
      baseUrl = iamURLToken;
      postUri = "/";
    }
    
    String bodyValue = "grant_type=authorization_code";
    bodyValue += "&code="          + code;
    bodyValue += "&client_id="     + iamClientId;
    bodyValue += "&code_verifier=" + codeVerifier;
    bodyValue += "&redirect_uri="  + iamRedirectURI;
    
    System.out.println("POST " + iamURLToken + " " + bodyValue + "...");
    
    IAMTokenResponse response = null;
    try {
      WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
      
      response = webClient
        .post()
        .uri(postUri)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .bodyValue(bodyValue)
        .retrieve()
        .bodyToMono(IAMTokenResponse.class)
        .timeout(Duration.ofSeconds(20))
        .block();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    
    System.out.println("POST " + iamURLToken + " " + bodyValue + " -> " + response);
    if(response == null) {
      System.out.println("IAM.requestToken(" + code + "," + state + ") -> null (response is null)");
      return null;
    }
    System.out.println("IAM.requestToken(" + code + "," + state + ") -> " + response);
    return response;
  }
  
  public static IAMUserInfo getUserInfo(String token) {
    if(token == null || token.length() == 0) {
      System.out.println("IAM.getUserInfo(" + token + ") -> null (invalid token)");
      return null;
    }
    String iamURLUserInfo = System.getenv(IAM_URL_USERINFO);
    if(iamURLUserInfo == null || iamURLUserInfo.length() == 0) {
      iamURLUserInfo = DEF_URL_USERINFO;
    }
    if(iamURLUserInfo == null || iamURLUserInfo.length() < 8) {
      System.out.println("IAM.getUserInfo(" + token + ") -> null (" + IAM_URL_USERINFO + "=" + iamURLUserInfo + ")");
      return null;
    }
    String baseUrl = null;
    String postUri = null;
    int lastSep = iamURLUserInfo.lastIndexOf('/');
    if(lastSep > 0) {
      baseUrl = iamURLUserInfo.substring(0, lastSep);
      postUri = iamURLUserInfo.substring(lastSep);
    }
    else {
      baseUrl = iamURLUserInfo;
      postUri = "/";
    }
    
    IAMUserInfo response = null;
    try {
      WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
      
      response = webClient
        .get()
        .uri(postUri)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .retrieve()
        .bodyToMono(IAMUserInfo.class)
        .timeout(Duration.ofSeconds(20))
        .block();
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    System.out.println("GET " + iamURLUserInfo + " -> " + response);
    if(response == null) {
      System.out.println("IAM.getUserInfo(" + token + ") -> null (response is null)");
      return null;
    }
    System.out.println("IAM.getUserInfo(" + token + ") -> " + response);
    return response;
  }
  
  public static String getSubject(String token) {
    System.out.println("IAM.getSubject(" + token + ")...");
    if(token == null || token.length() == 0) {
      System.out.println("IAM.getSubject(" + token + ") -> null");
      return null;
    }
    String result = null;
    if(token.length() == 16 && Character.isLetter(token.charAt(0))) {
      result = token.toUpperCase();
      System.out.println("IAM.getSubject(" + token + ") -> " + result);
      return result;
    }
    DecodedJWT decodedJWT = JWT.decode(token);
    if(decodedJWT != null) {
      result = decodedJWT.getSubject();
    }
    if(result == null || result.length() == 0) {
      return null;
    }
    result = result.toUpperCase();
    System.out.println("IAM.getSubject(" + token + ") -> " + result);
    return result;
  }
  
  public static String toBase64URLEncoded(byte[] arrayOfBytes) {
    if(arrayOfBytes == null || arrayOfBytes.length == 0) {
      return "";
    }
    String b64 = Base64.getEncoder().encodeToString(arrayOfBytes);
    if(b64 == null) {
      return "";
    }
    // Base64 URL Variant
    return b64.replace('+', '-').replace('/', '_').replace("=", "");
  }
  
  public static String getServletPath(String state) {
    if(state == null || state.length() == 0) {
      return null;
    }
    return mapStatesPath.get(state);
  }
  
  public static String generateState() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] arrayOfRandomBytes = new byte[16];
    secureRandom.nextBytes(arrayOfRandomBytes);
    return toBase64URLEncoded(arrayOfRandomBytes);
  }
  
  public static String generateCodeVerifier() {
    SecureRandom secureRandom = new SecureRandom();
    byte[] arrayOfRandomBytes = new byte[32];
    secureRandom.nextBytes(arrayOfRandomBytes);
    return toBase64URLEncoded(arrayOfRandomBytes);
  }
  
  public static String generateCodeChallange(String codeVerifier) {
    try {
      byte[] bytes = codeVerifier.getBytes("US-ASCII");
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(bytes, 0, bytes.length);
      byte[] digest = messageDigest.digest();
      return toBase64URLEncoded(digest);
    }
    catch(Exception ex) {
      ex.printStackTrace();
    }
    return "";
  }
}
