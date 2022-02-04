provider "azurerm" {
  features {}
}

data "azurerm_key_vault" "sscs_key_vault" {
  name                = "${var.product}-${var.env}"
  resource_group_name = "${var.product}-${var.env}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
  name        = "microservicekey-sscs-hearings-api"
}

resource "azurerm_key_vault_secret" "s2s_secret_sscs_hearings_api" {
  name         = "s2s-secret-sscs-hearings-api"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.sscs_key_vault.id
}
