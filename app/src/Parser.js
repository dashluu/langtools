import { useState } from 'react';
import './Parser.css'
import OutputView from './OutputView';
import Editor from './Editor';

function getParseOutput(result) {
    if (result.success) {
        // Format JSON string if the input is parsed successfully
        return JSON.stringify(result.output, null, 4);
    }
    return result.output;
}

export default function Parser() {
    const [result, setResult] = useState({success: false, output: ""});
    return (
        <div id='masterContainer' className='containers'>
            <div id='editorContainer' className='containers'>
                <Editor callback={(data) => {setResult(data);}} />
            </div>
            <div id='outputViewContainer' className='containers'>
                <OutputView success={result.success} output={getParseOutput(result)} />
            </div>
        </div>
    );
}