import XCTest
@testable import DisqoPlugin

class DisqoTests: XCTestCase {
    func testSdkStatusIsUnavailableOnIOS() {
        let implementation = Disqo()
        let result = implementation.sdkStatus()

        XCTAssertEqual(result["available"] as? Bool, false)
    }

    func testGetPluginVersion() {
        let implementation = Disqo()
        let result = implementation.getPluginVersion()

        XCTAssertEqual("8.0.0", result)
    }
}
