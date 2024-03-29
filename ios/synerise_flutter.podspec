#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint synerise_flutter.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'synerise_flutter'
  s.version          = '0.0.1'
  s.summary          = 'Synerise flutter plugin.'
  s.description      = <<-DESC
Synerise flutter plugin.
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.dependency 'SyneriseSDK'
  s.platform = :ios, '10.0'

  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES',
    'ONLY_ACTIVE_ARCH' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'arm64' }
  s.swift_version = '5.0'
end
