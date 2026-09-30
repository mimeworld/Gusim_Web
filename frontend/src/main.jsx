import React from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import './styles.css'; import './overrides.css'; import { Provider } from './context'; import { Header } from './components';
import Home from './pages/Home'; import About from './pages/About'; import Activities from './pages/Activities'; import Share from './pages/Share'; import UploadPhoto from './pages/UploadPhoto'; import MyPhotos from './pages/MyPhotos'; import MyPage from './pages/MyPage'; import Equipment from './pages/Equipment';
function App(){return <Provider><Header/><Routes><Route path="/" element={<Home/>}/><Route path="/about" element={<About/>}/><Route path="/activities" element={<Activities/>}/><Route path="/share" element={<Share/>}/><Route path="/share/upload" element={<UploadPhoto/>}/><Route path="/mypage" element={<MyPage/>}/><Route path="/mypage/photos" element={<MyPhotos/>}/><Route path="/equipment" element={<Equipment/>}/></Routes><footer>© 2024 GUDO SIMDO · GACHON UNIVERSITY PHOTOGRAPHY CLUB</footer></Provider>}; createRoot(document.getElementById('root')).render(<BrowserRouter><App/></BrowserRouter>);
