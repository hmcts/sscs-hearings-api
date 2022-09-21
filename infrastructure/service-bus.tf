#HMC to Hearings API
locals {
  namespace_name      = "hmc-servicebus-${var.env}"
  resource_group_name = "hmc-shared-${var.env}"
  topic_name          = "hmc-to-cft-${var.env}"
  key_vault_secrets = [
    "hmc-servicebus-connection-string",
    "hmc-servicebus-shared-access-key"
  ]
}

module "servicebus_subscription" {
  source              = "git@github.com:hmcts/terraform-module-servicebus-subscription?ref=master"
  name                = "hmc-to-sscs-subscription-${var.env}"
  namespace_name      = local.namespace_name
  topic_name          = local.topic_name
  resource_group_name = local.resource_group_name
}

resource "azurerm_servicebus_subscription_rule" "topic_filter_rule_sscs" {
  name            = "${local.namespace_name}-subscription-rule-bba3"
  subscription_id = module.servicebus_subscription.id
  filter_type     = "CorrelationFilter"

  correlation_filter {
    properties = {
      hmctsServiceId = "BBA3"
    }
  }
}

data "azurerm_key_vault" "hmc" {
  name                = "hmc-${var.env}"
  resource_group_name = local.resource_group_name
}

data "azurerm_key_vault_secret" "hmc" {
  for_each     = toset(local.key_vault_secrets)
  key_vault_id = data.azurerm_key_vault.hmc.id
  name         = each.key
}

resource "azurerm_key_vault_secret" "shared_access_key" {
  for_each     = data.azurerm_key_vault_secret.this
  name         = each.value.name
  value        = each.value.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}

