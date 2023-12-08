variable "product" {
  description = "Project short name (sscs)"
}

variable "component" {
  description = "Current Repository Name"
}

variable "location" {
  default = "UK South"
}

variable "env" {
  description = "Current Environment"
}

variable "subscription" {
  description = "Target Azure Subscription"
}

variable "common_tags" {
  type = map(string)
}

variable "deploymentId" {
  default = ""
}
