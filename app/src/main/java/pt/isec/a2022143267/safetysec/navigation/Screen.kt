package pt.isec.a2022143267.safetysec.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    // Authentication screens
    object Login : Screen("login")
    object MFA : Screen("mfa")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Settings : Screen("settings")

    // Monitor screens
    object MonitorDashboard : Screen("monitor_dashboard")
    object MonitorSettings : Screen("monitor_settings")
    object MonitorProtectedDetails : Screen("monitor_protected_details/{protectedId}") {
        fun createRoute(protectedId: String) = "monitor_protected_details/$protectedId"
    }
    object MonitorProtectedList : Screen("monitor_protected_list")
    object MonitorRules : Screen("monitor_rules")
    object MonitorRuleDetail : Screen("monitor_rule_detail/{ruleId}") {
        fun createRoute(ruleId: String) = "monitor_rule_detail/$ruleId"
    }
    object MonitorCreateRule : Screen("monitor_create_rule/{protectedId}") {
        fun createRoute(protectedId: String) = "monitor_create_rule/$protectedId"
    }
    object MonitorAlerts : Screen("monitor_alerts")
    object MonitorAddProtected : Screen("monitor_add_protected")

    // Protected screens
    object ProtectedDashboard : Screen("protected_dashboard")
    object ProtectedProfile : Screen("protected_profile")
    object ProtectedRules : Screen("protected_rules")
    object ProtectedTimeWindows : Screen("protected_time_windows")
    object ProtectedMonitors : Screen("protected_monitors")
    object ProtectedAuthorizations : Screen("protected_authorizations")
    object ProtectedHistory : Screen("protected_history")
    object ProtectedGenerateOTP : Screen("protected_generate_otp")

    // Alert screen (shared)
    object AlertScreen : Screen("alert_screen/{alertId}") {
        fun createRoute(alertId: String) = "alert_screen/$alertId"
    }

    // Monitor Alert Detail screen
    object MonitorAlertDetail : Screen("monitor_alert_detail/{alertId}") {
        fun createRoute(alertId: String) = "monitor_alert_detail/$alertId"
    }

    // Monitor Protected Alert History screen
    object MonitorProtectedAlertHistory : Screen("monitor_protected_alert_history/{protectedId}") {
        fun createRoute(protectedId: String) = "monitor_protected_alert_history/$protectedId"
    }
}

