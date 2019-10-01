//@flow
import React from "react";
import { translate } from "react-i18next";
import type { DisplayedUser } from "@scm-manager/ui-types";
import TagGroup from "./TagGroup";

type Props = {
  members: string[],
  memberListChanged: (string[]) => void,
  t: string => string
};

class MemberNameTagGroup extends React.Component<Props> {
  render() {
    const { members, t } = this.props;
    const membersExtended = members.map(id => {
      return { id, displayName: id, mail: "" };
    });
    return (
      <TagGroup
        items={membersExtended}
        label={t("group.members")}
        helpText={t("groupForm.help.memberHelpText")}
        onRemove={this.removeEntry}
      />
    );
  }

  removeEntry = (membersExtended: DisplayedUser[]) => {
    const members = membersExtended.map(function(item) {
      return item["id"];
    });
    this.props.memberListChanged(members);
  };
}

export default translate("groups")(MemberNameTagGroup);