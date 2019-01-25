// @flow
import React from "react";
import { translate } from "react-i18next";
import { Autocomplete, SubmitButton } from "@scm-manager/ui-components";
import RoleSelector from "./RoleSelector";
import type {
  AvailableRepositoryPermissions,
  PermissionCollection,
  PermissionCreateEntry,
  SelectValue
} from "@scm-manager/ui-types";
import * as validator from "./permissionValidation";
import { findMatchingRoleName } from "../modules/permissions";

type Props = {
  t: string => string,
  availablePermissions: AvailableRepositoryPermissions,
  createPermission: (permission: PermissionCreateEntry) => void,
  loading: boolean,
  currentPermissions: PermissionCollection,
  groupAutoCompleteLink: string,
  userAutoCompleteLink: string
};

type State = {
  name: string,
  verbs: string[],
  groupPermission: boolean,
  valid: boolean,
  value?: SelectValue
};

class CreatePermissionForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      name: "",
      verbs: props.availablePermissions.availableRoles[0].verbs,
      groupPermission: false,
      valid: true,
      value: undefined
    };
  }

  permissionScopeChanged = event => {
    const groupPermission = event.target.value === "GROUP_PERMISSION";
    this.setState({
      groupPermission: groupPermission,
      valid: validator.isPermissionValid(
        this.state.name,
        groupPermission,
        this.props.currentPermissions
      )
    });
    this.setState({ ...this.state, groupPermission });
  };

  loadUserAutocompletion = (inputValue: string) => {
    return this.loadAutocompletion(this.props.userAutoCompleteLink, inputValue);
  };

  loadGroupAutocompletion = (inputValue: string) => {
    return this.loadAutocompletion(
      this.props.groupAutoCompleteLink,
      inputValue
    );
  };

  loadAutocompletion(url: string, inputValue: string) {
    const link = url + "?q=";
    return fetch(link + inputValue)
      .then(response => response.json())
      .then(json => {
        return json.map(element => {
          const label = element.displayName
            ? `${element.displayName} (${element.id})`
            : element.id;
          return {
            value: element,
            label
          };
        });
      });
  }
  renderAutocompletionField = () => {
    const { t } = this.props;
    if (this.state.groupPermission) {
      return (
        <Autocomplete
          loadSuggestions={this.loadGroupAutocompletion}
          valueSelected={this.groupOrUserSelected}
          value={this.state.value}
          label={t("permission.group")}
          noOptionsMessage={t("permission.autocomplete.no-group-options")}
          loadingMessage={t("permission.autocomplete.loading")}
          placeholder={t("permission.autocomplete.group-placeholder")}
        />
      );
    }
    return (
      <Autocomplete
        loadSuggestions={this.loadUserAutocompletion}
        valueSelected={this.groupOrUserSelected}
        value={this.state.value}
        label={t("permission.user")}
        noOptionsMessage={t("permission.autocomplete.no-user-options")}
        loadingMessage={t("permission.autocomplete.loading")}
        placeholder={t("permission.autocomplete.user-placeholder")}
      />
    );
  };

  groupOrUserSelected = (value: SelectValue) => {
    this.setState({
      value,
      name: value.value.id,
      valid: validator.isPermissionValid(
        value.value.id,
        this.state.groupPermission,
        this.props.currentPermissions
      )
    });
  };

  render() {
    const { t, availablePermissions, loading } = this.props;

    const { verbs } = this.state;

    const availableRoleNames = availablePermissions.availableRoles.map(
      r => r.name
    );
    const matchingRole = findMatchingRoleName(availablePermissions, verbs);

    return (
      <div>
        <hr />
        <h2 className="subtitle">
          {t("permission.add-permission.add-permission-heading")}
        </h2>
        <form onSubmit={this.submit}>
          <div className="control">
            <label className="radio">
              <input
                type="radio"
                name="permission_scope"
                checked={!this.state.groupPermission}
                value="USER_PERMISSION"
                onChange={this.permissionScopeChanged}
              />
              {t("permission.user-permission")}
            </label>
            <label className="radio">
              <input
                type="radio"
                name="permission_scope"
                value="GROUP_PERMISSION"
                checked={this.state.groupPermission}
                onChange={this.permissionScopeChanged}
              />
              {t("permission.group-permission")}
            </label>
          </div>

          <div className="columns">
            <div className="column is-three-quarters">
              {this.renderAutocompletionField()}
            </div>
            <div className="column is-one-quarter">
              <RoleSelector
                availableRoles={availableRoleNames}
                label={t("permission.role")}
                helpText={t("permission.help.roleHelpText")}
                handleRoleChange={this.handleRoleChange}
                role={
                  matchingRole
                    ? matchingRole
                    : availablePermissions.availableRoles[0].name
                }
              />
            </div>
          </div>
          <div className="columns">
            <div className="column">
              <SubmitButton
                label={t("permission.add-permission.submit-button")}
                loading={loading}
                disabled={!this.state.valid || this.state.name === ""}
              />
            </div>
          </div>
        </form>
      </div>
    );
  }

  submit = e => {
    this.props.createPermission({
      name: this.state.name,
      verbs: this.state.verbs,
      groupPermission: this.state.groupPermission
    });
    this.removeState();
    e.preventDefault();
  };

  removeState = () => {
    this.setState({
      name: "",
      verbs: this.props.availablePermissions.availableRoles[0].verbs,
      groupPermission: false,
      valid: true
    });
  };

  handleRoleChange = (role: string) => {
    const selectedRole = this.findAvailableRole(role);
    this.setState({
      verbs: selectedRole.verbs
    });
  };

  findAvailableRole = (roleName: string) => {
    return this.props.availablePermissions.availableRoles.find(
      role => role.name === roleName
    );
  };
}

export default translate("repos")(CreatePermissionForm);
