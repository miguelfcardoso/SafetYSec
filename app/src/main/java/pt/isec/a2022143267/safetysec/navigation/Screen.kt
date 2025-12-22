package pt.isec.a2022143267.safetysec.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    // Authentication screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Monitor screens
    object MonitorDashboard : Screen("monitor_dashboard")
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
    object ProtectedAuthorizations : Screen("protected_authorizations")
    object ProtectedHistory : Screen("protected_history")
    object ProtectedGenerateOTP : Screen("protected_generate_otp")

    // Alert screen (shared)
    object AlertScreen : Screen("alert_screen/{alertId}") {
        fun createRoute(alertId: String) = "alert_screen/$alertId"
    }
}

