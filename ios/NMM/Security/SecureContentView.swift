import SwiftUI
import UIKit

/// Wraps sensitive financial content using UITextField secure text entry
/// to prevent screenshots and screen recording (equivalent to Android FLAG_SECURE).
struct SecureContentView<Content: View>: UIViewRepresentable {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    func makeUIView(context: Context) -> SecureContainerView {
        let container = SecureContainerView()
        let hostingController = UIHostingController(rootView: content)
        hostingController.view.backgroundColor = .clear
        hostingController.view.translatesAutoresizingMaskIntoConstraints = false
        container.setHostedView(hostingController.view)
        return container
    }

    func updateUIView(_ uiView: SecureContainerView, context: Context) {}
}

final class SecureContainerView: UIView {
    private let secureField = UITextField()
    private var hostedView: UIView?

    override init(frame: CGRect) {
        super.init(frame: frame)
        secureField.isSecureTextEntry = true
        secureField.isUserInteractionEnabled = false
        addSubview(secureField)
        secureField.translatesAutoresizingMaskIntoConstraints = false
        NSLayoutConstraint.activate([
            secureField.topAnchor.constraint(equalTo: topAnchor),
            secureField.leadingAnchor.constraint(equalTo: leadingAnchor),
            secureField.trailingAnchor.constraint(equalTo: trailingAnchor),
            secureField.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
    }

    required init?(coder: NSCoder) { fatalError("init(coder:) has not been implemented") }

    func setHostedView(_ view: UIView) {
        hostedView?.removeFromSuperview()
        hostedView = view
        if let layer = secureField.layer.sublayers?.first {
            layer.addSublayer(view.layer)
        } else {
            secureField.addSubview(view)
            NSLayoutConstraint.activate([
                view.topAnchor.constraint(equalTo: secureField.topAnchor),
                view.leadingAnchor.constraint(equalTo: secureField.leadingAnchor),
                view.trailingAnchor.constraint(equalTo: secureField.trailingAnchor),
                view.bottomAnchor.constraint(equalTo: secureField.bottomAnchor)
            ])
        }
    }
}
