import Foundation

@objc public class Disqo: NSObject {
    @objc public func getPluginVersion() -> String {
        return "8.0.0"
    }

    @objc public func sdkStatus() -> [String: Any] {
        return [
            "available": false,
            "missingClasses": [],
            "message": "Disqo Pulse is only available on Android."
        ]
    }
}
