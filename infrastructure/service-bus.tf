#HMC to Hearings API
module "servicebus-subscription" {
  source                = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                  = "hmc-to-sscs-subscription-${var.env}"
  namespace_name        = "hmc-servicebus-${var.env}"
  topic_name            = "hmc-to-cft-${var.env}"
  resource_group_name   = "hmc-shared-${var.env}"
}

data "azurerm_key_vault" "hmc-key-vault" {
  name                = "hmc-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
}

data "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-connection-string"
}

resource "azurerm_key_vault_secret" "hmc-servicebus-connection-string" {
  name         = "hmc-servicebus-connection-string"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-connection-string.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}

data "azurerm_key_vault_secret" "hmc-servicebus-shared-access-key" {
  key_vault_id = data.azurerm_key_vault.hmc-key-vault.id
  name         = "hmc-servicebus-shared-access-key"
}

resource "azurerm_key_vault_secret" "sscs-hmc-servicebus-hared-access-key" {
  name         = "hmc-servicebus-shared-access-key"
  value        = data.azurerm_key_vault_secret.hmc-servicebus-shared-access-key.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}
  
data "azurerm_servicebus_namespace" "hmc" {
  name                = "hmc-servicebus-${var.env}" 
  resource_group_name = "hmc-shared-${var.env}"
}
  
resource "azurerm_servicebus_topic" "hmc-to-cft" {
  name                = "hmc-to-cft"
  namespace_id        = "data.azurerm_servicebus_namespace.servicebus_subscription_rule_sscs.id
  enable_partitioning = true
}

resource "azurerm_servicebus_subscription" "hmc-to-cft" {
  name               = "hmc-to-cft-${var.env}"
  topic_id           = azurerum_servicebus_topic.servicebus_subscription_rule_sscs.id
  max_delivery_count = 1
}

resource "azurerm_servicebus_subscription_rule" "servicebus_subscription_rule_sscs" {
  name            = "hmc-servicebus-subscription-rule=bba3"
  subscription_id = azurerm_servicebus_subscription.hmc_to_cft.id
  filter_type     = "CorrelationFilter"
  
  correlation_filter {
    properties     = {
      hmctsServiceId = "BBA3"    
    }
  }
}
