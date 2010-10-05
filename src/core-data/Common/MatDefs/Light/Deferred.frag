#define ATTENUATION
//#define HQ_ATTENUATION

varying vec2 texCoord;

uniform sampler2D m_DiffuseData;
uniform sampler2D m_SpecularData;
uniform sampler2D m_NormalData;
uniform sampler2D m_DepthData;

uniform vec3 m_FrustumCorner;
uniform vec2 m_FrustumNearFar;

uniform vec4 g_LightColor;
uniform vec4 g_LightPosition;
uniform vec3 g_CameraPosition;

uniform mat4 m_ViewProjectionMatrixInverse;

#ifdef COLORRAMP
  uniform sampler2D m_ColorRamp;
#endif

float tangDot(in vec3 v1, in vec3 v2){
    float d = dot(v1,v2);
    #ifdef V_TANGENT
        d = 1.0 - d*d;
        return step(0.0, d) * sqrt(d);
    #else
        return d;
    #endif
}

float lightComputeDiffuse(in vec3 norm, in vec3 lightdir, in vec3 viewdir){
    #ifdef MINNAERT
        float NdotL = max(0.0, dot(norm, lightdir));
        float NdotV = max(0.0, dot(norm, viewdir));
        return NdotL * pow(max(NdotL * NdotV, 0.1), -1.0) * 0.5;
    #else
        return max(0.0, dot(norm, lightdir));
    #endif
}

float lightComputeSpecular(in vec3 norm, in vec3 viewdir, in vec3 lightdir, in float shiny){
    #ifdef LOW_QUALITY
       // Blinn-Phong
       // Note: preferably, H should be computed in the vertex shader
       vec3 H = (viewdir + lightdir) * vec3(0.5);
       return pow(max(tangDot(H, norm), 0.0), shiny);
    #elif defined(WARDISO)
        // Isotropic Ward
        vec3 halfVec = normalize(viewdir + lightdir);
        float NdotH  = max(0.001, tangDot(norm, halfVec));
        float NdotV  = max(0.001, tangDot(norm, viewdir));
        float NdotL  = max(0.001, tangDot(norm, lightdir));
        float a      = tan(acos(NdotH));
        float p      = max(shiny/128.0, 0.001);
        return NdotL * (1.0 / (4.0*3.14159265*p*p)) * (exp(-(a*a)/(p*p)) / (sqrt(NdotV * NdotL)));
    #else
       // Standard Phong
       vec3 R = reflect(-lightdir, norm);
       return pow(max(tangDot(R, viewdir), 0.0), shiny);
    #endif
}

vec2 computeLighting(in vec3 wvPos, in vec3 wvNorm, in vec3 wvViewDir, in vec4 wvLightDir, in float shiny){
   float diffuseFactor = lightComputeDiffuse(wvNorm, wvLightDir.xyz, wvViewDir);
   float specularFactor = 1.0;
   specularFactor = //step(0.001, m_Shininess) * min(1.0, diffuseFactor * 10.0)
                   lightComputeSpecular(wvNorm, wvViewDir, wvLightDir.xyz, shiny);

   #ifdef HQ_ATTENUATION
    float att = clamp(1.0 - g_LightPosition.w * length(lightVec), 0.0, 1.0);
   #else
    float att = wvLightDir.w;
   #endif

   //specularFactor = 0.0;

   return vec2(diffuseFactor, specularFactor) * vec2(att);
}


vec3 decodeNormal(in vec4 enc){
    vec4 nn = enc * vec4(2.0,2.0,0.0,0.0) + vec4(-1.0,-1.0,1.0,-1.0);
    float l = dot(nn.xyz, -nn.xyw);
    nn.z = l;
    nn.xy *= sqrt(l);
    return nn.xyz * vec3(2.0) + vec3(0.0,0.0,-1.0);
}

vec3 getPosition(in vec2 newTexCoord){
  //Reconstruction from depth
  float depth = texture2D(m_DepthData, newTexCoord).r;
  //depth = (2.0 * m_FrustumNearFar.x)
  /// (m_FrustumNearFar.y + m_FrustumNearFar.x - depth * (m_FrustumNearFar.y-m_FrustumNearFar.x));

  //one frustum corner method
  //float x = mix(-m_FrustumCorner.x, m_FrustumCorner.x, newTexCoord.x);
  //float y = mix(-m_FrustumCorner.y, m_FrustumCorner.y, newTexCoord.y);

  //return depth * vec3(x, y, m_FrustumCorner.z);
  vec4 pos;
  pos.xy = (newTexCoord * vec2(2.0)) - vec2(1.0);
  pos.z  = depth;
  pos.w  = 1.0;
  pos    = m_ViewProjectionMatrixInverse * pos;
  //pos   /= pos.w;
  return pos.xyz;
}

// JME3 lights in world space
void lightComputeDir(in vec3 worldPos, in vec4 color, in vec4 position, out vec4 lightDir){
    float posLight = step(0.5, color.w);
    vec3 tempVec = position.xyz * sign(posLight - 0.5) - (worldPos * posLight);
    #ifdef ATTENUATION
     float dist = length(tempVec);
     lightDir.w = clamp(1.0 - position.w * dist * posLight, 0.0, 1.0);
     lightDir.xyz = tempVec / vec3(dist);
     #ifdef HQ_ATTENUATION
       lightVec = tempVec;
     #endif
    #else
     lightDir = vec4(normalize(tempVec), 1.0);
    #endif
}

void main(){
    vec2 newTexCoord = texCoord;

    vec3 worldPosition = getPosition(newTexCoord);
    vec3 viewDir  = normalize(g_CameraPosition - worldPosition);

    // ***********************
    // Read from textures
    // ***********************
    vec4 normalInfo = texture2D(m_NormalData, newTexCoord);
    normalInfo.zw = vec2(0.0);
    vec3 normal = decodeNormal(normalInfo);
    #if !defined(LOW_QUALITY) && !defined(V_TANGENT)
       normal = normalize(normal);
    #endif
   
    vec4 diffuseColor  = texture2D(m_DiffuseData,  newTexCoord);
    vec4 specularColor = texture2D(m_SpecularData, newTexCoord);

    vec4 lightDir;
    lightComputeDir(worldPosition, g_LightColor, g_LightPosition, lightDir);

    vec2 light = computeLighting(worldPosition, normal, viewDir, lightDir, specularColor.w*128.0);
    #ifdef COLORRAMP
        diffuseColor.rgb  *= texture2D(m_ColorRamp, vec2(light.x, 0.0)).rgb;
        specularColor.rgb *= texture2D(m_ColorRamp, vec2(light.y, 0.0)).rgb;
    #endif

    gl_FragColor = vec4(light.x * diffuseColor.xyz + light.y * specularColor.xyz, 1.0);
    gl_FragColor.xyz *= g_LightColor.xyz;

   // gl_FragColor = vec4(viewDir, 1.0);
    //vec3 len = vec3(length(normal));
    //gl_FragColor = vec4( (normalize(wvPosition.xyz)*len + vec3(1.0)) * vec3(0.5) , 1.0 );
}
