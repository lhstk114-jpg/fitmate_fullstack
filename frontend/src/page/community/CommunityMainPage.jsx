import React from "react";
import CommunityLeft from "../../components/community/CommunityLeft";
import CommunityMain from "../../components/community/CommunityMain";
import "../../css/Community/CommunityMainPage.css";
import "../../css/Community/CommunityLeft.css";
import "../../css/Community/CommunityMain.css";

const CommunityMainPage = () => {
  return (
    <div className="community-wrapper">
      <CommunityLeft />
      <CommunityMain />
    </div>
  );
};

export default CommunityMainPage;
