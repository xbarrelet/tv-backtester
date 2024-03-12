import React, { useState, useEffect } from "react";
import './App.css';

import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';

import { BottomNavigation, BottomNavigationAction } from '@mui/material';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import RocketLaunchIcon from '@mui/icons-material/RocketLaunch';

import background from './img/wallpaper.png';



const App = () => {
  const [value, setValue] = React.useState(0);


  return (
    <div className="main">

      <BottomNavigation
        showLabels
        value={value}
        onChange={(event, newValue) => {
          setValue(newValue);
        }}
        style={{
          backgroundColor: "transparent",
        }}
        sx={{color: "white",}}
      >
        <BottomNavigationAction label="Currently tested strats" icon={<RocketLaunchIcon />} />
        <BottomNavigationAction label="Backtesting results" icon={<AccountBalanceIcon />} />
      </BottomNavigation>
    </div>
  );
};

export default App;