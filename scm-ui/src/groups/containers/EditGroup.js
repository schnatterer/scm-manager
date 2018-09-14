//@flow
import React from "react";
import { connect } from "react-redux";
import GroupForm from "../components/GroupForm";
import { modifyGroup, fetchGroup } from "../modules/groups";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import type { Group } from "@scm-manager/ui-types";
import { isModifyGroupPending, getModifyGroupFailure } from "../modules/groups";
import { ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  group: Group,
  modifyGroup: (group: Group, callback?: () => void) => void,
  fetchGroup: (name: string) => void,
  history: History,
  loading?: boolean,
  error: Error
};

class EditGroup extends React.Component<Props> {
  groupModified = (group: Group) => () => {
    this.props.fetchGroup(group.name);
    this.props.history.push(`/group/${group.name}`);
  };

  modifyGroup = (group: Group) => {
    this.props.modifyGroup(group, this.groupModified(group));
  };

  render() {
    const { group, loading, error } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <GroupForm
          group={group}
          submitForm={group => {
            this.modifyGroup(group);
          }}
          loading={loading}
        />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isModifyGroupPending(state, ownProps.group.name);
  const error = getModifyGroupFailure(state, ownProps.group.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyGroup: (group: Group, callback?: () => void) => {
      dispatch(modifyGroup(group, callback));
    },
    fetchGroup: (name: string) => {
      dispatch(fetchGroup(name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(EditGroup));