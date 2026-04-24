import Foundation
import Capacitor

@objc(DisqoPlugin)
public class DisqoPlugin: CAPPlugin, CAPBridgedPlugin {
    private let implementation = Disqo()

    public let identifier = "DisqoPlugin"
    public let jsName = "Disqo"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getSdkStatus", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "start", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "updateAccessToken", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "clearAccessToken", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getServiceStateInfo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isServiceEnabled", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openAccessibilitySettings", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "refreshConfigs", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "send", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "resolveAccessTokenRequest", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "rejectAccessTokenRequest", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "resolveRefreshTokenRequest", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "rejectRefreshTokenRequest", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getPluginVersion", returnType: CAPPluginReturnPromise)
    ]

    private func rejectUnavailable(_ call: CAPPluginCall) {
        call.reject("Disqo Pulse is only available on Android.")
    }

    @objc func getSdkStatus(_ call: CAPPluginCall) {
        call.resolve(implementation.sdkStatus())
    }

    @objc func initialize(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func start(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func stop(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func updateAccessToken(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func clearAccessToken(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func getServiceStateInfo(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func isServiceEnabled(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func openAccessibilitySettings(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func refreshConfigs(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func send(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func resolveAccessTokenRequest(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func rejectAccessTokenRequest(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func resolveRefreshTokenRequest(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func rejectRefreshTokenRequest(_ call: CAPPluginCall) {
        rejectUnavailable(call)
    }

    @objc func getPluginVersion(_ call: CAPPluginCall) {
        call.resolve([
            "version": implementation.getPluginVersion()
        ])
    }
}
