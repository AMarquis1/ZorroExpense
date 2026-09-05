import SwiftUI
import FirebaseCore
import GoogleSignIn

private let googleSignInStartNotification = Notification.Name("com.marquis.zorroexpense.googleSignIn.start")
private let googleSignInTokenNotification = Notification.Name("com.marquis.zorroexpense.googleSignIn.token")
private let googleSignInFailureNotification = Notification.Name("com.marquis.zorroexpense.googleSignIn.failure")
private let googleSignInTokenKey = "idToken"
private let googleSignInCancelledKey = "cancelled"

class AppDelegate: NSObject, UIApplicationDelegate {
  func application(_ application: UIApplication,
                   didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
    FirebaseApp.configure()

    NotificationCenter.default.addObserver(
      self,
      selector: #selector(startGoogleSignIn),
      name: googleSignInStartNotification,
      object: nil
    )

    return true
  }

  func application(
    _ app: UIApplication,
    open url: URL,
    options: [UIApplication.OpenURLOptionsKey: Any] = [:]
  ) -> Bool {
    GIDSignIn.sharedInstance.handle(url)
  }

  @objc private func startGoogleSignIn() {
    guard let rootViewController = UIApplication.shared.connectedScenes
      .compactMap({ ($0 as? UIWindowScene)?.keyWindow })
      .first?.rootViewController else {
      postGoogleSignInFailure(cancelled: false)
      return
    }

    GIDSignIn.sharedInstance.configuration = GIDConfiguration(
      clientID: "10827675040-7070h3emq34njemgtkou42sgr77c9h1a.apps.googleusercontent.com"
    )
    GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { result, error in
      if let error {
        let cancelled = (error as NSError).code == GIDSignInError.canceled.rawValue
        self.postGoogleSignInFailure(cancelled: cancelled)
        return
      }

      guard let idToken = result?.user.idToken?.tokenString else {
        self.postGoogleSignInFailure(cancelled: false)
        return
      }

      NotificationCenter.default.post(
        name: googleSignInTokenNotification,
        object: nil,
        userInfo: [googleSignInTokenKey: idToken]
      )
    }
  }

  private func postGoogleSignInFailure(cancelled: Bool) {
    NotificationCenter.default.post(
      name: googleSignInFailureNotification,
      object: nil,
      userInfo: [googleSignInCancelledKey: cancelled]
    )
  }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
